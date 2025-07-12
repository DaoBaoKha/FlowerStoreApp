package com.example.flowerstoreproject.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("_id")
    private String id;

    @SerializedName("accountId")
    private Account accountId;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("items")
    private List<OrderItemDetail> items;

    @SerializedName("shippingFee")
    private double shippingFee;

    @SerializedName("addressShip")
    private String addressShip;

    @SerializedName("transactionId")
    private JsonElement transactionId;  // Changed from String to JsonElement

    @SerializedName("paymentCode")
    private String paymentCode;

    @SerializedName("proofOfDelivery")
    private String proofOfDelivery;

    @SerializedName("orderAt")
    private String orderAt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getAccountId() {
        return accountId;
    }

    public void setAccountId(Account accountId) {
        this.accountId = accountId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItemDetail> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetail> items) {
        this.items = items;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getAddressShip() {
        return addressShip;
    }

    public void setAddressShip(String addressShip) {
        this.addressShip = addressShip;
    }

    // Updated transactionId getter to handle both string and object
    public String getTransactionId() {
        if (transactionId == null) return null;

        if (transactionId.isJsonPrimitive()) {
            return transactionId.getAsString();
        } else if (transactionId.isJsonObject()) {
            JsonObject obj = transactionId.getAsJsonObject();
            // Try common field names for transaction ID
            if (obj.has("id")) {
                return obj.get("id").getAsString();
            } else if (obj.has("transactionId")) {
                return obj.get("transactionId").getAsString();
            } else if (obj.has("_id")) {
                return obj.get("_id").getAsString();
            }
            // If no common field found, return the first string value
            for (String key : obj.keySet()) {
                if (obj.get(key).isJsonPrimitive() && obj.get(key).getAsJsonPrimitive().isString()) {
                    return obj.get(key).getAsString();
                }
            }
        }
        return null;
    }

    public void setTransactionId(JsonElement transactionId) {
        this.transactionId = transactionId;
    }

    // Helper method to get transaction object (if needed)
    public JsonObject getTransactionIdAsObject() {
        if (transactionId != null && transactionId.isJsonObject()) {
            return transactionId.getAsJsonObject();
        }
        return null;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public String getProofOfDelivery() {
        return proofOfDelivery;
    }

    public void setProofOfDelivery(String proofOfDelivery) {
        this.proofOfDelivery = proofOfDelivery;
    }

    public String getOrderAt() {
        return orderAt;
    }

    public void setOrderAt(String orderAt) {
        this.orderAt = orderAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to get customer name
    public String getCustomerName() {
        if (accountId != null) {
            return accountId.getFullName();
        }
        return "Unknown";
    }

    // Helper method to get customer email
    public String getCustomerEmail() {
        if (accountId != null) {
            return accountId.getEmail();
        }
        return "Unknown";
    }

    // Helper method to get customer phone
    public String getCustomerPhone() {
        if (accountId != null) {
            return accountId.getPhone();
        }
        return "Unknown";
    }

    // Helper method to get total items count
    public int getTotalItemsCount() {
        if (items != null) {
            int count = 0;
            for (OrderItemDetail item : items) {
                count += item.getQuantity();
            }
            return count;
        }
        return 0;
    }
}