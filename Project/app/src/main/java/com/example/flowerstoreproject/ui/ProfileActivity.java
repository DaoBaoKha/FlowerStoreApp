package com.example.flowerstoreproject.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.ProfileService;
import com.example.flowerstoreproject.model.Profile;
import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private EditText etFullName, etEmail, etPhone;
    private ImageView ivAvatar;
    private Button btnSave, btnLogout;
    private SharedPreferences sharedPreferences;
    private LinearLayout homeLayout, cartLayout, ordersLayout, profileLayout;
    private ImageView homeIcon, cartIcon, ordersIcon, profileIcon;
    private TextView homeText, cartText, ordersText, profileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout); // Khởi tạo nút Logout

        // Khởi tạo thanh taskbar
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

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

        // Lấy dữ liệu từ Intent
        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String avatar = getIntent().getStringExtra("avatar");
        String role = getIntent().getStringExtra("role");

        // Hiển thị dữ liệu
        etFullName.setText(fullName != null ? fullName : "");
        etEmail.setText(email != null ? email : "");
        etPhone.setText(phone != null ? phone : "");
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .into(ivAvatar);
        }

        // Thiết lập sự kiện click cho các nút
        btnSave.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> logout()); // Thêm sự kiện cho nút Logout

        // Thiết lập sự kiện click cho thanh taskbar
        homeLayout.setOnClickListener(v -> navigateTo(0));
        cartLayout.setOnClickListener(v -> navigateTo(1));
        ordersLayout.setOnClickListener(v -> navigateTo(2));
        profileLayout.setOnClickListener(v -> navigateTo(3));

        // Đặt Profile là mặc định được chọn
        updateNavigationSelection(3);
    }

    private void saveProfile() {
        String token = sharedPreferences.getString("token", null);
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Profile updatedProfile = new Profile(fullName, email, phone, null, null);
        ProfileService profileService = RetrofitClient.getClient().create(ProfileService.class);
        Call<Profile> call = profileService.updateProfile("Bearer " + token, updatedProfile);

        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Profile profile = response.body();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("full_name", profile.getFullName());
                    editor.putString("email", profile.getEmail());
                    editor.putString("phone", profile.getPhone());
                    editor.apply();
                    Toast.makeText(ProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    etFullName.setText(profile.getFullName());
                    etEmail.setText(profile.getEmail());
                    etPhone.setText(profile.getPhone());
                } else {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi cập nhật profile", t);
            }
        });
    }

    private void logout() {
        // Xóa thông tin người dùng khỏi SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("full_name");
        editor.remove("email");
        editor.remove("phone");
        editor.apply();

        // Hiển thị thông báo đăng xuất
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Chuyển hướng về LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa stack hoạt động
        startActivity(intent);
        updateNavigationSelection(0); // Chọn tab Home (có thể bỏ nếu LoginActivity không có taskbar)
        overridePendingTransition(R.drawable.slide_in_right, R.drawable.slide_out_left);
        finish(); // Đóng ProfileActivity
    }

    private void navigateTo(int position) {
        Intent intent = null;
        switch (position) {
            case 0: // Home
                intent = new Intent(this, MainActivity.class);
                break;
            case 1: // Cart
                intent = new Intent(this, CartActivity.class);
                break;
            case 2: // Orders
                intent = new Intent(this, OrdersActivity.class);
                break;
            case 3: // Profile
                return; // Ở lại trang Profile
        }
        if (intent != null) {
            startActivity(intent);
            updateNavigationSelection(position);
            overridePendingTransition(R.drawable.slide_in_right, R.drawable.slide_out_left);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void updateNavigationSelection(int position) {
        // Reset background cho tất cả tab
        int defaultBackground = android.R.color.transparent; // Không background
        int selectedBackground = ContextCompat.getColor(this, R.color.gray_light); // Background xám nhạt

        homeLayout.setBackgroundColor(defaultBackground);
        cartLayout.setBackgroundColor(defaultBackground);
        ordersLayout.setBackgroundColor(defaultBackground);
        profileLayout.setBackgroundColor(defaultBackground);

        // Đặt background cho tab được chọn
        switch (position) {
            case 0: // Home
                homeLayout.setBackgroundColor(selectedBackground);
                break;
            case 1: // Cart
                cartLayout.setBackgroundColor(selectedBackground);
                break;
            case 2: // Orders
                ordersLayout.setBackgroundColor(selectedBackground);
                break;
            case 3: // Profile
                profileLayout.setBackgroundColor(selectedBackground);
                break;
        }
    }
}