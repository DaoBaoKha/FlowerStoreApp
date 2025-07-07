package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.CreateOrderResponse;
import com.example.flowerstoreproject.model.OrderRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OrderService {
    @POST("orders")
    Call<CreateOrderResponse> createOrder(
            @Header("Authorization") String token,
            @Body OrderRequest orderRequest
    );
}