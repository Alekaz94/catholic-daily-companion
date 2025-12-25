/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class responsible for CORS (Cross-Origin Resource Sharing).
 * <p>
 * This configuration defines which client origins are allowed to access
 * the API and which HTTP methods and headers are permitted.
 */
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class WebConfig implements WebMvcConfigurer {

    private String[] allowedOrigins;

    /**
     * Sets the allowed origins for CORS requests.
     *
     * @param allowedOrigins array of permitted origin URLs
     */
    public void setAllowedOrigins(String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Configures global CORS mappings for the application.
     * <p>
     * Allows configured origins to access API endpoints using
     * standard HTTP methods while supporting credentials.
     *
     * @param registry CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
