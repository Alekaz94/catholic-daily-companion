package com.alexandros.dailycompanion.security;

import com.alexandros.dailycompanion.service.ServiceHelper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;
    private final ServiceHelper serviceHelper;

    @Autowired
    public RateLimitFilter(JwtUtil jwtUtil, ServiceHelper serviceHelper) {
        this.jwtUtil = jwtUtil;
        this.serviceHelper = serviceHelper;
    }

    // Default bucket for general endpoints: 30 req/min per user/IP
    private Bucket createUserBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        30,
                        Refill.greedy(30, Duration.ofMinutes(1))
                ))
                .build();
    }

    // Stricter bucket for auth endpoints: 5 req/min per IP
    private Bucket createAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        5,
                        Refill.greedy(5, Duration.ofMinutes(1))
                ))
                .build();
    }

    // Cleanup idle buckets to save memory
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanupBuckets() {
        buckets.entrySet().removeIf(entry -> entry.getValue().getAvailableTokens() > 50);
    }

    private String getBucketKey(HttpServletRequest request) {
        String ip = serviceHelper.getClientIp(request);
        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/auth")) return "AUTH_" + ip;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String email = jwtUtil.extractEmail(authHeader.substring(7));
                return "USER_" + email;
            } catch (Exception ignored) {}
        }

        return "API_" + ip;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip refresh-token (or other exceptions)
        if (path.equals("/api/v1/auth/refresh-token")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bucketKey = getBucketKey(request);
        Bucket bucket;

        if (path.startsWith("/api/v1/auth")) {
            bucket = buckets.computeIfAbsent(bucketKey, k -> createAuthBucket());
        } else {
            bucket = buckets.computeIfAbsent(bucketKey, k -> createUserBucket());
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            logger.warn("Rate limit exceeded | key: {} | path: {}", bucketKey, path);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\"}");
        }
    }
}
