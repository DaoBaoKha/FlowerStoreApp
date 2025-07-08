package com.example.flowerstoreproject.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderListResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Order> data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Order> getData() {
        return data;
    }
}
