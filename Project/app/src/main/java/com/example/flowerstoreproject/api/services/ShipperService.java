package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.Shipper;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ShipperService {
    @GET("shippers")
    Call<List<Shipper>> getAllShippers();
}