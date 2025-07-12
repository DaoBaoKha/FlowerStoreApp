package com.example.flowerstoreproject.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.fragment.AdminCategoriesFragment;
import com.example.flowerstoreproject.fragment.AdminFlowersFragment;
import com.example.flowerstoreproject.fragment.AdminOrdersFragment;
import com.example.flowerstoreproject.ui.LoginActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboardActivity";
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Log.d(TAG, "onCreate: Admin Dashboard Activity initialized");

        // Kiểm tra quyền admin
        sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "");

        if (!"admin".equals(userRole)) {
            Log.w(TAG, "onCreate: Unauthorized access attempt, redirecting to login");
            Toast.makeText(this, "Access denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Lấy thông tin user
        String userName = sharedPreferences.getString("full_name", "Admin");
        Log.d(TAG, "onCreate: Welcome admin: " + userName);

        // Khởi tạo bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Load fragment mặc định (Flowers)
        if (savedInstanceState == null) {
            loadFragment(new AdminFlowersFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_flowers);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        if (item.getItemId() == R.id.nav_flowers) {
            fragment = new AdminFlowersFragment();
            Log.d(TAG, "onNavigationItemSelected: Flowers management selected");
        } else if (item.getItemId() == R.id.nav_categories) {
            fragment = new AdminCategoriesFragment();
            Log.d(TAG, "onNavigationItemSelected: Categories management selected");
        } else if (item.getItemId() == R.id.nav_dashboard) {
            // TODO: Implement AdminDashboardFragment
            // fragment = new AdminDashboardFragment();
            Log.d(TAG, "onNavigationItemSelected: Dashboard selected");
        } else if (item.getItemId() == R.id.nav_account) {
            // TODO: Implement AdminAccountFragment
            fragment = new AdminOrdersFragment();
            Log.d(TAG, "onNavigationItemSelected: Account selected");
        }

        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }

        return false;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        Log.d(TAG, "loadFragment: Fragment loaded - " + fragment.getClass().getSimpleName());
    }

    private void redirectToLogin() {
        // Xóa dữ liệu đăng nhập
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Chuyển về login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Xử lý back press - có thể hiển thị dialog xác nhận đăng xuất
        super.onBackPressed();
    }
}