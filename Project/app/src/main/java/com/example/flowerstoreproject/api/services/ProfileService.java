package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.Profile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Body;

public interface ProfileService {
    @GET("profile")
    Call<Profile> getProfile(@Header("Authorization") String authorization);

    @PUT("profile")
    Call<Profile> updateProfile(@Header("Authorization") String authorization, @Body Profile profile);
}