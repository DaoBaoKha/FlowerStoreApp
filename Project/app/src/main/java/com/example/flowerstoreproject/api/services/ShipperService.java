package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.Order;
import com.example.flowerstoreproject.model.OrderAssignRequest;
import com.example.flowerstoreproject.model.OrderListResponse;
import com.example.flowerstoreproject.model.Shipper;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ShipperService {

    @GET("shippers")
    Call<List<Shipper>> getAllShippers();

    @PUT("shippers/{orderId}/assign-shipper")
    Call<Object> assignOrder(@Path("orderId") String orderId, @Body OrderAssignRequest request);

    @GET("shippers/my-orders")
    Call<List<Order>> getMyOrders(@Header("Authorization") String authorization);

    @Multipart
    @PUT("shippers/{orderId}/complete-delivery")
    Call<Void> completeDelivery(@Header("Authorization") String authorization, @Path("orderId") String orderId, @Part MultipartBody.Part proofImage);
}