package com.example.flowerstoreproject.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Order {

    @SerializedName("_id")
    private String id;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("shippingFee")
    private double shippingFee;

    @SerializedName("orderAt")
    private String orderAt;

    @SerializedName("status")
    private String status;

    @SerializedName("addressShip")
    private String addressShip;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public String getOrderAt() {
        return orderAt;
    }

    public String getStatus() {
        return status;
    }

    public String getAddressShip() {
        return addressShip;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
