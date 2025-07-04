package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryService {
    @GET("categories")
    Call<List<Category>> getCategories();
}