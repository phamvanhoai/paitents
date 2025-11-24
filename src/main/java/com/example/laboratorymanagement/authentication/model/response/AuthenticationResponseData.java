package com.example.laboratorymanagement.authentication.model.response;

import java.util.Set;

public class AuthenticationResponseData {
    private String userId;
    private String username;
    private String email;
    private String roleCode;
    private Set<String> privilegeCodes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Set<String> getPrivilegeCodes() {
        return privilegeCodes;
    }

    public void setPrivilegeCodes(Set<String> privilegeCodes) {
        this.privilegeCodes = privilegeCodes;
    }
}
