package com.example.flowerstoreproject.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.AccountService;
import com.example.flowerstoreproject.model.ApiResponse;
import com.example.flowerstoreproject.model.Profile;
import com.example.flowerstoreproject.model.ProfileUpdateRequest;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private EditText etFullName, etEmail, etPhone;
    private Button btnSave, btnLogout;

    private String token;
    private String userId; // Thay accountId thành userId
    private SharedPreferences prefs;

    private AccountService accountService;

    // Navigation bar
    private LinearLayout homeLayout, cartLayout, ordersLayout, profileLayout;
    private ImageView homeIcon, cartIcon, ordersIcon, profileIcon;
    private TextView homeText, cartText, ordersText, profileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivAvatar = findViewById(R.id.ivAvatar);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);

        prefs = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        token = prefs.getString("token", null);
        userId = prefs.getString("user_id", null); // Sử dụng key "user_id"

        accountService = RetrofitClient.getClient().create(AccountService.class);

        Log.d("ProfileActivity", "onCreate - Token: " + token);
        Log.d("ProfileActivity", "onCreate - User ID: " + userId);

        if (token == null) {
            Log.e("ProfileActivity", "Token null -> Chuyển đến LoginActivity");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        // Gọi loadProfile để lấy dữ liệu từ API
        loadProfile();

        btnSave.setOnClickListener(v -> updateProfile());
        btnLogout.setOnClickListener(v -> logout());

        // Navigation setup
        homeLayout = findViewById(R.id.home_layout);
        cartLayout = findViewById(R.id.cart_layout);
        ordersLayout = findViewById(R.id.orders_layout);
        profileLayout = findViewById(R.id.profile_layout);

        homeIcon = findViewById(R.id.home_icon);
        cartIcon = findViewById(R.id.cart_icon);
        ordersIcon = findViewById(R.id.orders_icon);
        profileIcon = findViewById(R.id.profile_icon);

        homeText = findViewById(R.id.home_text);
        cartText = findViewById(R.id.cart_text);
        ordersText = findViewById(R.id.orders_text);
        profileText = findViewById(R.id.profile_text);

        profileIcon.setColorFilter(ContextCompat.getColor(this, R.color.primary_color));
        profileText.setTextColor(ContextCompat.getColor(this, R.color.primary_color));

        homeLayout.setOnClickListener(v -> navigateTo(0));
        cartLayout.setOnClickListener(v -> navigateTo(1));
        ordersLayout.setOnClickListener(v -> navigateTo(2));
        profileLayout.setOnClickListener(v -> navigateTo(3));
    }

    private void loadProfile() {
        Log.d("LoadProfile", "Token: " + token);

        if (token == null) {
            Log.e("LoadProfile", "Token null -> Hủy tải thông tin");
            Toast.makeText(this, "Không thể xác định tài khoản, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        Call<Profile> call = accountService.getProfile("Bearer " + token);
        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Log.d("LoadProfile", "onResponse - Status Code: " + response.code());
                Log.d("LoadProfile", "Response body: " + (response.body() != null ? response.body().toString() : "null"));

                if (response.isSuccessful() && response.body() != null) {
                    Profile profile = response.body();
                    Log.d("LoadProfile", "Profile data: fullName=" + profile.getFullName() + ", email=" + profile.getEmail() + ", phone=" + profile.getPhone() + ", avatar=" + profile.getAvatar() + ", role=" + profile.getRole());

                    etFullName.setText(profile.getFullName());
                    etEmail.setText(profile.getEmail());
                    etPhone.setText(profile.getPhone());

                    Glide.with(ProfileActivity.this)
                            .load(profile.getAvatar())
                            .placeholder(R.drawable.baseline_dashboard_24)
                            .into(ivAvatar);

                    // Lưu thông tin vào SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("fullName", profile.getFullName());
                    editor.putString("email", profile.getEmail());
                    editor.putString("phone", profile.getPhone());
                    editor.putString("avatar", profile.getAvatar());
                    editor.putString("role", profile.getRole());
                    editor.apply();
                    Log.d("LoadProfile", "Saved profile to SharedPreferences");

                    // Nếu userId vẫn null, cố gắng lấy từ GET /api/accounts/{id}
                    if (userId == null) {
                        Log.w("LoadProfile", "User ID null, attempting to fetch from GET /api/accounts/{id}");
                        fetchUserIdFromAccount();
                    }
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("LoadProfile", "Tải thất bại - Status Code: " + response.code() + ", errorBody: " + error);
                    } catch (IOException e) {
                        Log.e("LoadProfile", "IOException khi đọc errorBody", e);
                    }
                    if (response.code() == 401) {
                        Log.e("LoadProfile", "Phiên đăng nhập hết hạn");
                        Toast.makeText(ProfileActivity.this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
                        logout();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.e("LoadProfile", "onFailure: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserIdFromAccount() {
        // Giả sử userId có thể lấy từ token JWT
        if (token != null && token.split("\\.").length == 3) {
            try {
                String[] parts = token.split("\\.");
                String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
                org.json.JSONObject payloadJson = new org.json.JSONObject(payload);
                String idFromToken = payloadJson.optString("id");
                if (idFromToken != null && !idFromToken.isEmpty()) {
                    userId = idFromToken;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_id", userId);
                    editor.apply();
                    Log.d("LoadProfile", "Fetched user_id from JWT: " + userId);
                } else {
                    Log.e("LoadProfile", "No id found in JWT payload");
                }
            } catch (Exception e) {
                Log.e("LoadProfile", "Error decoding JWT for user_id: ", e);
            }
        }

        // Nếu vẫn không có userId, gọi GET /api/accounts/{id} (nếu biết id từ nguồn khác)
        if (userId == null) {
            Log.e("LoadProfile", "Cannot fetch user_id, logging out");
            Toast.makeText(this, "Không thể xác định tài khoản, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            logout();
        }
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Log.w("UpdateProfile", "Thiếu thông tin: fullName=" + fullName + ", email=" + email + ", phone=" + phone);
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy lại token và userId từ SharedPreferences
        token = prefs.getString("token", null);
        userId = prefs.getString("user_id", null);

        Log.d("UpdateProfile", "Token: " + token);
        Log.d("UpdateProfile", "User ID: " + userId);
        Log.d("UpdateProfile", "Sending: fullName=" + fullName + ", email=" + email + ", phone=" + phone);

        if (token == null || userId == null) {
            Log.e("UpdateProfile", "Token hoặc userId null -> Hủy cập nhật");
            Toast.makeText(this, "Không thể xác định tài khoản, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        ProfileUpdateRequest request = new ProfileUpdateRequest(fullName, email, phone);

        Call<ApiResponse<Profile>> call = accountService.updateProfile("Bearer " + token, userId, request);
        call.enqueue(new Callback<ApiResponse<Profile>>() {
            @Override
            public void onResponse(Call<ApiResponse<Profile>> call, Response<ApiResponse<Profile>> response) {
                Log.d("UpdateProfile", "onResponse - Status Code: " + response.code());
                Log.d("UpdateProfile", "Response body: " + (response.body() != null ? response.body().toString() : "null"));

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Profile updated = response.body().getData();
                    Log.d("UpdateProfile", "Cập nhật thành công: id=" + updated.getId() + ", fullName=" + updated.getFullName() + ", email=" + updated.getEmail() + ", phone=" + updated.getPhone() + ", avatar=" + updated.getAvatar() + ", role=" + updated.getRole());

                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    // Cập nhật SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_id", updated.getId());
                    editor.putString("fullName", updated.getFullName());
                    editor.putString("email", updated.getEmail());
                    editor.putString("phone", updated.getPhone());
                    editor.putString("avatar", updated.getAvatar());
                    editor.putString("role", updated.getRole());
                    editor.apply();
                    Log.d("UpdateProfile", "Saved updated profile to SharedPreferences");
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("UpdateProfile", "Cập nhật thất bại - Status Code: " + response.code() + ", errorBody: " + error);
                    } catch (IOException e) {
                        Log.e("UpdateProfile", "IOException khi đọc errorBody", e);
                    }
                    if (response.code() == 401) {
                        Log.e("UpdateProfile", "Phiên đăng nhập hết hạn");
                        Toast.makeText(ProfileActivity.this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
                        logout();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Profile>> call, Throwable t) {
                Log.e("UpdateProfile", "onFailure: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        Log.d("ProfileActivity", "Logging out, clearing SharedPreferences");
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateTo(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(this, MainActivity.class);
                break;
            case 1:
                intent = new Intent(this, CartActivity.class);
                break;
            case 2:
                intent = new Intent(this, OrdersActivity.class);
                break;
            case 3:
                return; // Đang ở đây rồi
        }

        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(R.drawable.slide_in_right, R.drawable.slide_out_left);
            finish();
        }
    }
}