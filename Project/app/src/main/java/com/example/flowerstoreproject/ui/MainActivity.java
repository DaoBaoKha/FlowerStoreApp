package com.example.flowerstoreproject.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.CategoryAdapter;
import com.example.flowerstoreproject.adapters.ProductAdapter;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.CategoryService;
import com.example.flowerstoreproject.api.services.ProfileService;
import com.example.flowerstoreproject.api.services.ProductService;
import com.example.flowerstoreproject.model.Category;
import com.example.flowerstoreproject.model.Product;
import com.example.flowerstoreproject.model.Profile;
import com.example.flowerstoreproject.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView categoryRecyclerView, productRecyclerView;
    private Button btnShowAll;
    private SharedPreferences sharedPreferences;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private List<Category> categories = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private LinearLayout homeLayout, cartLayout, ordersLayout, profileLayout;
    private ImageView homeIcon, cartIcon, ordersIcon, profileIcon;
    private TextView homeText, cartText, ordersText, profileText, cartBadge; // Đã khai báo cartBadge

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            // Khởi tạo views
            categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
            productRecyclerView = findViewById(R.id.productRecyclerView);
            btnShowAll = findViewById(R.id.btnShowAll);

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
            cartBadge = findViewById(R.id.cart_badge); // Thêm khởi tạo cartBadge

            // Khởi tạo SharedPreferences
            sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

            // Khởi tạo RecyclerView adapters
            categoryAdapter = new CategoryAdapter(this, categories, category -> {
                loadProducts(category.getId()); // Tải sản phẩm theo danh mục
            });
            categoryRecyclerView.setAdapter(categoryAdapter);
            categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            // Trong onCreate của MainActivity
            productAdapter = new ProductAdapter(this, products, product -> {
                CartManager.getInstance().addToCart(product);
                updateCartBadge(); // Cập nhật badge khi thêm vào giỏ
            });

            productRecyclerView.setAdapter(productAdapter);
            productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Thiết lập sự kiện click cho thanh taskbar
            homeLayout.setOnClickListener(v -> navigateTo(0));
            cartLayout.setOnClickListener(v -> navigateTo(1));
            ordersLayout.setOnClickListener(v -> navigateTo(2));
            profileLayout.setOnClickListener(v -> navigateTo(3));

            // Thiết lập sự kiện click cho nút "Tất cả"
            btnShowAll.setOnClickListener(v -> loadAllProducts());

            // Đặt Home là mặc định được chọn
            updateNavigationSelection(0);

            // Tải danh mục và tất cả sản phẩm khi khởi động
            loadCategories();
            loadAllProducts();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onCreate: ", e);
            Toast.makeText(this, "Lỗi tải MainActivity", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCategories() {
        CategoryService categoryService = RetrofitClient.getClient().create(CategoryService.class);
        Call<List<Category>> call = categoryService.getCategories();

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    categoryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Không tải được danh mục", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi tải danh mục", t);
            }
        });
    }

    private void loadProducts(String categoryId) {
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        Call<List<Product>> call = productService.getProductsByCategory(categoryId);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    products.clear();
                    products.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi tải sản phẩm", t);
            }
        });
    }

    private void loadAllProducts() {
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        Call<List<Product>> call = productService.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    products.clear();
                    products.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Không tải được tất cả sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi tải tất cả sản phẩm", t);
            }
        });
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
                String token = sharedPreferences.getString("token", null);
                if (token == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
                    intent = new Intent(this, LoginActivity.class);
                } else {
                    ProfileService profileService = RetrofitClient.getClient().create(ProfileService.class);
                    Call<Profile> call = profileService.getProfile("Bearer " + token);
                    call.enqueue(new Callback<Profile>() {
                        @Override
                        public void onResponse(Call<Profile> call, Response<Profile> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Profile profile = response.body();
                                Log.d(TAG, "Profile loaded: " + profile.getFullName() + ", " + profile.getEmail());
                                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("fullName", profile.getFullName());
                                profileIntent.putExtra("email", profile.getEmail());
                                profileIntent.putExtra("phone", profile.getPhone());
                                profileIntent.putExtra("avatar", profile.getAvatar());
                                profileIntent.putExtra("role", profile.getRole());
                                startActivity(profileIntent);
                                updateNavigationSelection(3);
                                overridePendingTransition(R.drawable.slide_in_right, R.drawable.slide_out_left);
                            } else {
                                Toast.makeText(MainActivity.this, "Không tải được thông tin profile", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Phản hồi không thành công: " + response.code());
                                if (response.code() == 401) {
                                    Toast.makeText(MainActivity.this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Profile> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Lỗi tải profile", t);
                        }
                    });
                    return;
                }
                break;
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

    // Phương thức để cập nhật badge trên icon giỏ hàng
    private void updateCartBadge() {
        runOnUiThread(() -> {
            int count = CartManager.getInstance().getCartItemCount();
            if (count > 0) {
                cartBadge.setText(String.valueOf(count));
                cartBadge.setVisibility(View.VISIBLE);
            } else {
                cartBadge.setVisibility(View.GONE);
            }
        });
    }
}