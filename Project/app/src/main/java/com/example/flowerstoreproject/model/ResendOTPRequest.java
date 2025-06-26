package com.example.flowerstoreproject.model;

public class ResendOTPRequest {
    private String email;

    public ResendOTPRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
