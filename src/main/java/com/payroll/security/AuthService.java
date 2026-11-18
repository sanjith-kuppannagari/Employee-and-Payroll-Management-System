package com.payroll.security;

import java.util.HashMap;

public class AuthService {
    private static final HashMap<String, String> CREDENTIALS = new HashMap<>();

    static {
        String user = System.getenv("ADMIN_USERNAME") != null ? System.getenv("ADMIN_USERNAME") : "admin";
        String pass = System.getenv("ADMIN_PASSWORD") != null ? System.getenv("ADMIN_PASSWORD") : "admin123";
        CREDENTIALS.put(user, pass);
    }

    public boolean authenticate(String username, String password) {
        return username != null && password != null && password.equals(CREDENTIALS.get(username));
    }
}
