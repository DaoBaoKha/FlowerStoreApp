package com.example.flowerstoreproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flowerstoreproject.api.AuthService;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.model.RegisterResponse;
import com.example.flowerstoreproject.model.ResendOTPRequest;
import com.example.flowerstoreproject.model.VerifyOTPRequest;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOTPActivity extends AppCompatActivity {
    private static final String TAG = "VerifyOTPActivity";
    private TextView tvOTPMessage, tvResendTimer, tvResendOTP;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private LinearLayout btnVerifyOTP;
    private ProgressBar progressBar;
    private AuthService authService;
    private String email;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        tvOTPMessage = findViewById(R.id.tvOTPMessage);
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        progressBar = findViewById(R.id.progressBar);
        tvResendTimer = findViewById(R.id.tvResendTimer);
        tvResendOTP = findViewById(R.id.tvResendOTP);

        authService = RetrofitClient.getClient().create(AuthService.class);

        // Lấy email từ Intent
        email = getIntent().getStringExtra("email");
        if (email != null) {
            tvOTPMessage.setText("OTP has been sent to " + email + " please check your email to verify OTP");
        } else {
            tvOTPMessage.setText("Error: Email not provided");
        }

        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOTP();
            }
        });

        tvResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendOTP();
            }
        });
    }

    private void verifyOTP() {
        if (email == null) {
            Toast.makeText(this, "Error: Email not provided", Toast.LENGTH_SHORT).show();
            return;
        }

        String otp1 = etOtp1.getText().toString().trim();
        String otp2 = etOtp2.getText().toString().trim();
        String otp3 = etOtp3.getText().toString().trim();
        String otp4 = etOtp4.getText().toString().trim();
        String otp5 = etOtp5.getText().toString().trim();
        String otp6 = etOtp6.getText().toString().trim();

        String otp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6;

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter a 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnVerifyOTP.setEnabled(false);

        VerifyOTPRequest verifyOTPRequest = new VerifyOTPRequest(email, otp);
        Call<RegisterResponse> call = authService.verifyOTP(verifyOTPRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnVerifyOTP.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    Log.d(TAG, "OTP verification successful: " + registerResponse.getMessage());
                    Toast.makeText(VerifyOTPActivity.this, "OTP Verified! " + registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyOTPActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "OTP verification failed: Unknown error";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading errorBody: ", e);
                        }
                    } else {
                        errorMessage = "OTP verification failed: Error code " + response.code();
                    }
                    Log.e(TAG, errorMessage);
                    Toast.makeText(VerifyOTPActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnVerifyOTP.setEnabled(true);
                Log.e(TAG, "OTP verification failed: ", t);
                Toast.makeText(VerifyOTPActivity.this, "OTP verification failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOTP() {
        if (email == null) {
            Toast.makeText(this, "Error: Email not provided", Toast.LENGTH_SHORT).show();
            return;
        }

        tvResendOTP.setEnabled(false);
        tvResendTimer.setVisibility(View.VISIBLE);

        // Gửi yêu cầu resend OTP
        ResendOTPRequest resendOTPRequest = new ResendOTPRequest(email);
        Call<RegisterResponse> call = authService.resendOTP(resendOTPRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    Log.d(TAG, "Resend OTP successful: " + registerResponse.getMessage());
                    Toast.makeText(VerifyOTPActivity.this, "OTP Resent! " + registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "Resend OTP failed: Unknown error";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading errorBody: ", e);
                        }
                    } else {
                        errorMessage = "Resend OTP failed: Error code " + response.code();
                    }
                    Log.e(TAG, errorMessage);
                    Toast.makeText(VerifyOTPActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    // Khôi phục trạng thái nếu resend thất bại
                    tvResendOTP.setEnabled(true);
                    tvResendTimer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e(TAG, "Resend OTP failed: ", t);
                Toast.makeText(VerifyOTPActivity.this, "Resend OTP failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Khôi phục trạng thái nếu resend thất bại
                tvResendOTP.setEnabled(true);
                tvResendTimer.setVisibility(View.GONE);
            }
        });

        // Bắt đầu đếm ngược 60 giây
        startResendTimer();
    }

    private void startResendTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                String time = String.format("%02d:%02d", minutes, seconds);
                String fullText = "Resend OTP in " + time;
                SpannableString spannableString = new SpannableString(fullText);
                // Tô màu xanh cho phần thời gian (01:00)
                spannableString.setSpan(new ForegroundColorSpan(0xFF4CAF50), fullText.length() - 5, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Giữ màu đen cho phần "Resend OTP in"
                spannableString.setSpan(new ForegroundColorSpan(0xFF212121), 0, fullText.length() - 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvResendTimer.setText(spannableString);
            }

            @Override
            public void onFinish() {
                tvResendTimer.setVisibility(View.GONE);
                tvResendOTP.setEnabled(true);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}