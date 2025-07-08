package com.example.flowerstoreproject.api.services;

import com.example.flowerstoreproject.model.PaymentResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PaymentService {

    @POST("payments/create-payment-link")
    Call<PaymentResponse> createPaymentLink(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );
}
