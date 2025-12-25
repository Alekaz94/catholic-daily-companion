/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.AppVersionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for providing application version information.
 * <p>
 * This endpoint allows client applications to determine:
 * <ul>
 *     <li>The latest available app version</li>
 *     <li>The minimum supported app version</li>
 *     <li>The store URL for updating the app</li>
 * </ul>
 * It can be used to prompt users to update their app when necessary.
 */
@RestController
@RequestMapping("/api/v1/app")
public class AppVersionController {


    /**
     * The latest available version of the application.
     * Used to notify users about optional updates.
     */
    @Value("${latest.version}")
    private String latestVersion;

    /**
     * The minimum supported version required for the app to function correctly.
     * Versions below this value should prompt a forced update.
     */
    @Value("${minimum.supported.version}")
    private String minimumSupportedVersion;

    /**
     * URL for the application's store listing where updates can be downloaded.
     */
    @Value("${store.url}")
    private String storeUrl;

    /**
     * Returns application version information for client-side version checks.
     *
     * @return {@link AppVersionResponse} containing the latest version,
     *         minimum supported version, and store URL
     */
    @GetMapping("/version")
    public AppVersionResponse getVersion() {
        return new AppVersionResponse(latestVersion, minimumSupportedVersion, storeUrl);
    }
}
