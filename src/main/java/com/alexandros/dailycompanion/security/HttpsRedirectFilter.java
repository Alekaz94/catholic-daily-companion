/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class HttpsRedirectFilter implements Filter {

    private final Environment env;

    public HttpsRedirectFilter(Environment env) {
        this.env = env;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String proto = request.getHeader("X-Forwarded-Proto");
        if ("http".equals(proto)) {
            String redirectUrl = "https://" + request.getServerName() + request.getRequestURI();
            if (request.getQueryString() != null) {
                redirectUrl += "?" + request.getQueryString();
            }
            response.sendRedirect(redirectUrl);
            return;
        }

        chain.doFilter(req, res);
    }
}
