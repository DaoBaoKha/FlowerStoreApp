package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.Flower;
import com.example.flowerstoreproject.model.FlowerRequest;
import com.example.flowerstoreproject.model.FlowerUpdateRequest;
import com.example.flowerstoreproject.model.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FlowerService {

    @POST("flowers")
    Call<ApiResponse<Flower>> createFlower(
            @Header("Authorization") String token,
            @Body FlowerRequest flowerRequest
    );

    @GET("flowers")
    Call<List<Flower>> getAllFlowers();

    @GET("flowers/category/{categoryId}")
    Call<List<Flower>> getFlowersByCategory(@Path("categoryId") String categoryId);

    @GET("flowers/{id}")
    Call<Flower> getFlowerById(@Path("id") String id);

    @PUT("flowers/{id}")
    Call<ApiResponse<Flower>> updateFlower(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body FlowerUpdateRequest flowerUpdateRequest
    );

    @DELETE("flowers/{id}")
    Call<ApiResponse<Void>> deleteFlower(
            @Header("Authorization") String token,
            @Path("id") String id
    );
}