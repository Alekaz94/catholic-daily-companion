/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.enums;

public enum Roles {
    ADMIN,
    USER;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
