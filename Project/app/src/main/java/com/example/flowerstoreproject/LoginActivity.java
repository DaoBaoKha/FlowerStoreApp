package com.example.flowerstoreproject;

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
import com.example.flowerstoreproject.api.AuthService;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.model.LoginRequest;
import com.example.flowerstoreproject.model.LoginResponse;
import org.json.JSONObject;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

        authService = RetrofitClient.getClient().create(AuthService.class);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<LoginResponse> call = authService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d(TAG, "Login successful: " + loginResponse.getEmail());

                    // Decode JWT token to extract role
                    String role = ""; // Default role if decoding fails or role not found
                    String token = loginResponse.getToken();
                    if (token != null && !token.isEmpty()) {
                        try {
                            // Split JWT token and decode payload
                            String[] parts = token.split("\\.");
                            if (parts.length == 3) {
                                String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
                                JSONObject payloadJson = new JSONObject(payload);
                                role = payloadJson.optString("role", "Guest");
                                Log.d(TAG, "Extracted role: " + role);
                            } else {
                                Log.e(TAG, "Invalid JWT token format");
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

                    // Show success message and navigate to MainActivity
                    Toast.makeText(LoginActivity.this, "Login Successful! Role: " + role, Toast.LENGTH_SHORT).show();
                    try {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting MainActivity: ", e);
                        Toast.makeText(LoginActivity.this, "Error navigating to MainActivity", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Login failed: Response code " + response.code());
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.e(TAG, "Login failed: ", t);
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}