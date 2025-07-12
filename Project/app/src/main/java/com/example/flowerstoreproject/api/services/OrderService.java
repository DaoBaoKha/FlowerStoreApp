package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.CreateOrderResponse;
import com.example.flowerstoreproject.model.Order;
import com.example.flowerstoreproject.model.OrderListResponse;
import com.example.flowerstoreproject.model.OrderRequest;
import com.example.flowerstoreproject.model.UpdateOrderStatusRequest;
import com.example.flowerstoreproject.model.UpdateOrderStatusResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OrderService {
    @POST("orders")
    Call<CreateOrderResponse> createOrder(
            @Header("Authorization") String token,
            @Body OrderRequest orderRequest
    );

    @GET("orders")
    Call<OrderListResponse> getOrders(@Header("Authorization") String token);
    @PATCH("orders/{id}/status")
    Call<UpdateOrderStatusResponse> updateOrderStatus(
            @Header("Authorization") String authorization,
            @Path("id") String orderId,
            @Body UpdateOrderStatusRequest request
    );
}