package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.GeocodingResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingService {
    @GET("geocode/json")
    Call<GeocodingResponse> getLocation(
            @Query("address") String address,
            @Query("key") String apiKey
    );
}
