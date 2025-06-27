package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProductService {
    @GET("flowers/category/{categoryId}")
    Call<List<Product>> getProductsByCategory(@Path("categoryId") String categoryId);
}