package com.example.flowerstoreproject.model;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    private boolean success;
    private String message;
    private PaymentData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public PaymentData getData() {
        return data;
    }
}
