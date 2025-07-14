package com.example.flowerstoreproject.model;

public class OrderAssignRequest {
    private final String shipperId;

    public OrderAssignRequest(String shipperId) {
        this.shipperId = shipperId;
    }

    public String getShipperId() {
        return shipperId;
    }
}