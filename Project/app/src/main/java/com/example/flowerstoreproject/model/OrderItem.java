package com.example.flowerstoreproject.model;

public class OrderItem {
    private String flowerId;
    private int quantity;

    public OrderItem(String flowerId, int quantity) {
        this.flowerId = flowerId;
        this.quantity = quantity;
    }

    // Getter & Setter
    public String getFlowerId() {
        return flowerId;
    }

    public void setFlowerId(String flowerId) {
        this.flowerId = flowerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
