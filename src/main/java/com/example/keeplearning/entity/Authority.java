package com.example.keeplearning.entity;

import org.springframework.security.core.GrantedAuthority;

public class Authority implements GrantedAuthority {

    public static class Role
    {
        public static final String Sutdent = "Sutdent";
        public static final String Teacher = "Teacher";
        public static final String Admin = "Admin";
    }

    private String role;

    public Authority(String role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role;
    }
    
}
