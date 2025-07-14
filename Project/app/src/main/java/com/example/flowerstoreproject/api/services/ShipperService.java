package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.OrderAssignRequest;
import com.example.flowerstoreproject.model.Shipper;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ShipperService {

    @GET("shippers")
    Call<List<Shipper>> getAllShippers();

    @PUT("shippers/{orderId}/assign-shipper")
    Call<Object> assignOrder(@Path("orderId") String orderId, @Body OrderAssignRequest request);
}