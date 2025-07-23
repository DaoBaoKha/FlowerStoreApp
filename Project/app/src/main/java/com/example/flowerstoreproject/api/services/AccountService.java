package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.ApiResponse;
import com.example.flowerstoreproject.model.Profile;
import com.example.flowerstoreproject.model.ProfileUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AccountService {

    @GET("profile")
    Call<Profile> getProfile(
            @Header("Authorization") String authToken
    );

    @PUT("accounts/{id}")
    Call<ApiResponse<Profile>> updateProfile(
            @Header("Authorization") String authToken,
            @Path("id") String id,
            @Body ProfileUpdateRequest body
    );

    @GET("accounts/{id}")
    Call<ApiResponse<Profile>> getAccountById(
            @Header("Authorization") String authToken,
            @Path("id") String id
    );

    @PUT("accounts/{id}")
    Call<ApiResponse<Profile>> updateAccount(
            @Header("Authorization") String authToken,
            @Path("id") String id,
            @Body ProfileUpdateRequest body
    );
}