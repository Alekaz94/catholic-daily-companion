package com.alexandros.dailycompanion.enums;

public enum Roles {
    ADMIN,
    USER;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
