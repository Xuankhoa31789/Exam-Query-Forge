package com.eqf.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {
    private static final Map<String, String> users = new ConcurrentHashMap<>();

    static {
        // Demo user for testing
        users.put("demo", "password123");
    }

    public static Map<String, String> getUsers() {
        return users;
    }
}
