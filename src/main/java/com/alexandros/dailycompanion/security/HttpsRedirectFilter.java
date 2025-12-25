/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpsRedirectFilter extends OncePerRequestFilter {

    private final Environment env;

    public HttpsRedirectFilter(Environment env) {
        this.env = env;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String proto = request.getHeader("X-Forwarded-Proto");
        if ("http".equals(proto)) {
            String redirectUrl = "https://" + request.getServerName() + request.getRequestURI();
            if (request.getQueryString() != null) {
                redirectUrl += "?" + request.getQueryString();
            }
            response.sendRedirect(redirectUrl);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
