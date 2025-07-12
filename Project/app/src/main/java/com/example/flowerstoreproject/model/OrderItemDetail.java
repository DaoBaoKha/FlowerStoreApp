package com.example.flowerstoreproject.model;

import com.google.gson.annotations.SerializedName;

public class OrderItemDetail {
    @SerializedName("flowerId")
    private Flower flowerId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("_id")
    private String id;

    @SerializedName("actualPrice")
    private double actualPrice;

    // Getters and setters
    public Flower getFlowerId() {
        return flowerId;
    }

    public void setFlowerId(Flower flowerId) {
        this.flowerId = flowerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(double actualPrice) {
        this.actualPrice = actualPrice;
    }
}