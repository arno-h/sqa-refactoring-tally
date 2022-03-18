package com.example.service.impl;

public class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isBlank(String str) {
        return "".equals(str);
    }
}
