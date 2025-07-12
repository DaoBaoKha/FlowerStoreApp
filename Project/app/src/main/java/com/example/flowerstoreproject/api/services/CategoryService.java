package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.ApiResponse;
import com.example.flowerstoreproject.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryService {
    @GET("categories")
    Call<List<Category>> getCategories();
    @GET("categories/{id}")
    Call<ApiResponse<Category>> getCategoryById(@Path("id") int id);

    @POST("categories")
    Call<ApiResponse<Category>> createCategory(
            @Header("Authorization") String token,
            @Body Category category
    );

    @PUT("categories/{id}")
    Call<ApiResponse<Category>> updateCategory(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body Category category
    );

    @DELETE("categories/{id}")
    Call<ApiResponse<Void>> deleteCategory(
            @Header("Authorization") String token,
            @Path("id") String id
    );
}