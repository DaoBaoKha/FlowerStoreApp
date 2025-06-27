package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.LoginRequest;
import com.example.flowerstoreproject.model.LoginResponse;
import com.example.flowerstoreproject.model.RegisterRequest;
import com.example.flowerstoreproject.model.RegisterResponse;
import com.example.flowerstoreproject.model.ResendOTPRequest;
import com.example.flowerstoreproject.model.VerifyOTPRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @POST("auth/verify-otp")
    Call<RegisterResponse> verifyOTP(@Body VerifyOTPRequest verifyOTPRequest);

    @POST("auth/resend-otp")
    Call<RegisterResponse> resendOTP(@Body ResendOTPRequest verifyOTPRequest);
}
