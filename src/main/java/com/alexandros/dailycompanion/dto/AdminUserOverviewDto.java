/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import org.springframework.data.domain.Page;

public record AdminUserOverviewDto(UserDto user,
                                   int feedbackCount,
                                   Page<FeedbackDto> feedbacks){}
