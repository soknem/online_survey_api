package com.setec.online_survey.domain;

public enum UserRole {
    ROLE_ADMIN,
    ROLE_USER;
    
    // Optional utility method for easier lookup
    public String getAuthority() {
        return this.name();
    }
}