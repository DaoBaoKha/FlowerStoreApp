package com.example.flowerstoreproject.model;

import java.util.List;

public class OrderRequest {
    private String addressShip;
    private double shippingFee;
    private List<OrderItem> items;

    public OrderRequest(String addressShip, double shippingFee, List<OrderItem> items) {
        this.addressShip = addressShip;
        this.shippingFee = shippingFee;
        this.items = items;
    }

    // Getter & Setter
    public String getAddressShip() {
        return addressShip;
    }

    public void setAddressShip(String addressShip) {
        this.addressShip = addressShip;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
