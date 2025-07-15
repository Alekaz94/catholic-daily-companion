package com.alexandros.dailycompanion.Enum;

public enum Roles {
    ADMIN,
    USER;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
