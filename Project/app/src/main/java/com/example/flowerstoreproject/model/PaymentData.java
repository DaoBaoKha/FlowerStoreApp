package com.example.flowerstoreproject.model;

import com.google.gson.annotations.SerializedName;

public class PaymentData {

    @SerializedName("checkoutUrl")
    private String checkoutUrl;

    @SerializedName("paymentCode")
    private String paymentCode;

    @SerializedName("transactionId")
    private String transactionId;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("paymentLinkId")
    private String paymentLinkId;

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }
}
