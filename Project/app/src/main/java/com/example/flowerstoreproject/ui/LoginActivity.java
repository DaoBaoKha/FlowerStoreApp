package com.example.flowerstoreproject.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.services.AuthService;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.model.LoginRequest;
import com.example.flowerstoreproject.model.LoginResponse;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private LinearLayout btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private AuthService authService;
    private SharedPreferences sharedPreferences;
    private static final int MAX_RETRIES = 3;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: Activity initialized");

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
        Log.d(TAG, "Views initialized: etEmail, etPassword, btnLogin, tvRegister, progressBar");

        sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        Log.d(TAG, "SharedPreferences initialized: FlowerShopPrefs");

        authService = RetrofitClient.getClient().create(AuthService.class);
        Log.d(TAG, "AuthService initialized with Retrofit client");

        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "btnLogin onClick: Login button clicked");
            login();
        });

        tvRegister.setOnClickListener(v -> {
            Log.d(TAG, "tvRegister onClick: Register text clicked, navigating to RegisterActivity");
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.d(TAG, "login: Attempting login with email: " + email + ", password length: " + password.length());

        if (email.isEmpty() || password.isEmpty()) {
            Log.w(TAG, "login: Validation failed - Empty email or password");
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        Log.d(TAG, "login: ProgressBar shown, btnLogin disabled, preparing to send login request");

        performLoginWithRetry(email, password);
    }

    private void performLoginWithRetry(String email, String password) {
        Log.d(TAG, "performLoginWithRetry: Starting login attempt, retry count: " + retryCount);
        LoginRequest loginRequest = new LoginRequest(email, password);
        Log.d(TAG, "performLoginWithRetry: Login request created with payload: " + loginRequest.toString());

        Call<LoginResponse> call = authService.login(loginRequest);
        Log.d(TAG, "performLoginWithRetry: Enqueuing login call to server");

        // Thêm timeout handler
        new android.os.Handler().postDelayed(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) {
                Log.e(TAG, "performLoginWithRetry: Timeout after 120 seconds, no response from server");
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login timed out, please try again", Toast.LENGTH_SHORT).show();
            }
        }, 120000); // 120 giây

        // Thêm log kiểm tra sau 5 giây
        new android.os.Handler().postDelayed(() -> {
            Log.d(TAG, "performLoginWithRetry: Checking status after 5 seconds, progressBar visible: " + (progressBar.getVisibility() == View.VISIBLE));
        }, 5000); // 5 giây

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(TAG, "onResponse: Thread: " + Thread.currentThread().getName());
                Log.d(TAG, "onResponse: Received raw response: " + (response.body() != null ? response.body().toString() : "null"));
                Log.d(TAG, "onResponse: Received response from server, code: " + response.code());
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.d(TAG, "onResponse: ProgressBar hidden, btnLogin enabled");

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "onResponse: Login successful");
                    LoginResponse loginResponse = response.body();
                    String role = loginResponse.getRole();
                    Log.d(TAG, "onResponse: User data - Id=" + loginResponse.getId() +
                            ", Email=" + loginResponse.getEmail() +
                            ", FullName=" + loginResponse.getFullName() +
                            ", Phone=" + loginResponse.getPhone() +
                            ", Token=" + loginResponse.getToken() +
                            ", Role=" + role);

                    // Decode JWT token
                    String token = loginResponse.getToken();
                    if (token != null && !token.isEmpty()) {
                        Log.d(TAG, "onResponse: Decoding JWT token");
                        try {
                            String[] parts = token.split("\\.");
                            if (parts.length == 3) {
                                String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
                                JSONObject payloadJson = new JSONObject(payload);
                                String jwtRole = payloadJson.optString("role", role);
                                Log.d(TAG, "onResponse: Decoded role from JWT: " + jwtRole);
                                if (!jwtRole.equals(role)) {
                                    Log.w(TAG, "onResponse: Role mismatch - JSON=" + role + ", JWT=" + jwtRole);
                                }
                            } else {
                                Log.e(TAG, "onResponse: Invalid JWT token format, parts length: " + parts.length);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onResponse: Error decoding JWT token: ", e);
                        }
                    } else {
                        Log.e(TAG, "onResponse: Token is null or empty");
                    }

                    // Save to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_id", loginResponse.getId());
                    editor.putString("full_name", loginResponse.getFullName() != null ? loginResponse.getFullName() : "Guest");
                    editor.putString("email", loginResponse.getEmail());
                    editor.putString("phone", loginResponse.getPhone());
                    editor.putString("token", token);
                    editor.putString("role", role);
                    editor.apply();
                    Log.d(TAG, "onResponse: User data saved to SharedPreferences: Role=" + role);

                    Toast.makeText(LoginActivity.this, "Login Successful! Role: " + role, Toast.LENGTH_SHORT).show();
                    if(role.equals("admin")){
                        Intent intent = new Intent(LoginActivity.this, com.example.flowerstoreproject.ui.AdminDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    Log.d(TAG, "onResponse: Navigating to MainActivity and finishing LoginActivity");
                } else {
                    Log.e(TAG, "onResponse: Login failed, response code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "onResponse: Error body: " + errorBody);
                            Toast.makeText(LoginActivity.this, "Error: " + errorBody, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "onResponse: Error reading errorBody: ", e);
                            Toast.makeText(LoginActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "onResponse: No error body available");
                        Toast.makeText(LoginActivity.this, "Invalid credentials or server error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Thread: " + Thread.currentThread().getName());
                Log.e(TAG, "onFailure: Login failed due to error: " + t.getClass().getSimpleName() + " - " + (t.getMessage() != null ? t.getMessage() : "No message"), t);
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.d(TAG, "onFailure: ProgressBar hidden, btnLogin enabled");

                if (t instanceof java.net.SocketTimeoutException && retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.w(TAG, "onFailure: Timeout detected, retrying (" + retryCount + "/" + MAX_RETRIES + ") after 1 second");
                    new android.os.Handler().postDelayed(() -> performLoginWithRetry(email, password), 1000);
                } else {
                    Log.e(TAG, "onFailure: Max retries reached or non-timeout error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
                    Toast.makeText(LoginActivity.this, "Login failed: " + (t.getMessage() != null ? t.getMessage() : "Unknown error") + " (Max retries reached)", Toast.LENGTH_SHORT).show();
                    retryCount = 0;
                }
            }
        });
    }
}