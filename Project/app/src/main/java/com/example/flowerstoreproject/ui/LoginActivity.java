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

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        Log.d(TAG, "SharedPreferences initialized: FlowerShopPrefs");

        authService = RetrofitClient.getClient().create(AuthService.class);
        Log.d(TAG, "AuthService initialized with Retrofit client");

        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            login();
        });

        tvRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register text clicked, navigating to RegisterActivity");
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.d(TAG, "Login attempt with email: " + email + ", password length: " + password.length());

        if (email.isEmpty() || password.isEmpty()) {
            Log.w(TAG, "Validation failed: Empty email or password");
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        Log.d(TAG, "ProgressBar shown, btnLogin disabled, sending login request");

        performLoginWithRetry(email, password);
    }

    private void performLoginWithRetry(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<LoginResponse> call = authService.login(loginRequest);
        Log.d(TAG, "Login request created with payload: " + loginRequest.toString());

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.d(TAG, "Received response, code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String role = loginResponse.getRole(); // Lấy role từ JSON
                    Log.d(TAG, "Login successful: Email=" + loginResponse.getEmail() +
                            ", FullName=" + loginResponse.getFullName() +
                            ", Token=" + loginResponse.getToken() +
                            ", Role=" + role);

                    // Decode JWT token to verify role (optional)
                    String token = loginResponse.getToken();
                    if (token != null && !token.isEmpty()) {
                        try {
                            String[] parts = token.split("\\.");
                            if (parts.length == 3) {
                                String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
                                JSONObject payloadJson = new JSONObject(payload);
                                String jwtRole = payloadJson.optString("role", role);
                                if (!jwtRole.equals(role)) {
                                    Log.w(TAG, "Role mismatch: JSON=" + role + ", JWT=" + jwtRole);
                                }
                                Log.d(TAG, "Decoded role from JWT: " + jwtRole);
                            } else {
                                Log.e(TAG, "Invalid JWT token format, parts length: " + parts.length);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error decoding JWT token: ", e);
                        }
                    } else {
                        Log.e(TAG, "Token is null or empty");
                    }

                    // Save user data to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_id", loginResponse.getId());
                    editor.putString("full_name", loginResponse.getFullName() != null ? loginResponse.getFullName() : "Guest");
                    editor.putString("email", loginResponse.getEmail());
                    editor.putString("phone", loginResponse.getPhone());
                    editor.putString("token", token);
                    editor.putString("role", role);
                    editor.apply();
                    Log.d(TAG, "User data saved to SharedPreferences: Role=" + role);

                    Toast.makeText(LoginActivity.this, "Login Successful! Role: " + role, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Login failed: Response code " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody: ", e);
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Invalid credentials or server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.e(TAG, "Login failed due to network or server error: ", t);
                if (t instanceof java.net.SocketTimeoutException && retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.w(TAG, "Timeout detected, retrying (" + retryCount + "/" + MAX_RETRIES + ")");
                    performLoginWithRetry(email, password); // Thử lại
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage() + " (Max retries reached)", Toast.LENGTH_SHORT).show();
                    retryCount = 0; // Reset retry count
                }
            }
        });
    }
}