package com.example.flowerstoreproject.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.OrderAdapter;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.OrderService;
import com.example.flowerstoreproject.api.services.PaymentService;
import com.example.flowerstoreproject.model.Order;
import com.example.flowerstoreproject.model.OrderListResponse;
import com.example.flowerstoreproject.model.PaymentResponse;
import com.example.flowerstoreproject.utils.CartManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orders = new ArrayList<>();
    private String token;
    private LinearLayout homeLayout, cartLayout, ordersLayout, profileLayout;
    private TextView homeText, cartText, ordersText, profileText, cartBadge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Khởi tạo views
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        homeLayout = findViewById(R.id.home_layout);
        cartLayout = findViewById(R.id.cart_layout);
        ordersLayout = findViewById(R.id.orders_layout);
        profileLayout = findViewById(R.id.profile_layout);
        homeText = findViewById(R.id.home_text);
        cartText = findViewById(R.id.cart_text);
        ordersText = findViewById(R.id.orders_text);
        profileText = findViewById(R.id.profile_text);
        cartBadge = findViewById(R.id.cart_badge);

        // Thiết lập RecyclerView
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(orders);
        ordersRecyclerView.setAdapter(orderAdapter);

        // Cập nhật badge giỏ hàng
        updateCartBadge();

        // Lấy token
        SharedPreferences sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải danh sách đơn hàng
        fetchOrders();

        // Thiết lập sự kiện click cho thanh taskbar
        homeLayout.setOnClickListener(v -> navigateTo(0));
        cartLayout.setOnClickListener(v -> navigateTo(1));
        ordersLayout.setOnClickListener(v -> navigateTo(2));
        profileLayout.setOnClickListener(v -> navigateTo(3));

        // Đặt Orders là mặc định được chọn
        updateNavigationSelection(2);
    }

    private void fetchOrders() {
        OrderService orderService = RetrofitClient.getClient().create(OrderService.class);
        orderService.getOrders("Bearer " + token).enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(Call<OrderListResponse> call, Response<OrderListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    orders = response.body().getData();
                    orderAdapter = new OrderAdapter(orders);
                    ordersRecyclerView.setAdapter(orderAdapter);
                    orderAdapter.setOnPayClickListener(orderId -> createPaymentLink(orderId));
                } else {
                    Toast.makeText(OrdersActivity.this, "Không thể tải đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderListResponse> call, Throwable t) {
                Log.e("OrdersActivity", "Lỗi mạng", t);
                Toast.makeText(OrdersActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPaymentLink(String orderId) {
        PaymentService paymentService = RetrofitClient.getClient().create(PaymentService.class);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("orderId", orderId);
        requestBody.put("returnUrl", "myapp://payment/success");
        requestBody.put("cancelUrl", "myapp://payment/cancel");

        paymentService.createPaymentLink("Bearer " + token, requestBody)
                .enqueue(new Callback<PaymentResponse>() {
                    @Override
                    public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            String checkoutUrl = response.body().getData().getCheckoutUrl();
                            if (checkoutUrl != null) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                                startActivity(browserIntent);
                            } else {
                                Toast.makeText(OrdersActivity.this, "Không nhận được URL thanh toán", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(OrdersActivity.this, "Không thể tạo link thanh toán", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PaymentResponse> call, Throwable t) {
                        Toast.makeText(OrdersActivity.this, "Lỗi khi tạo link thanh toán: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                return; // Ở lại trang Orders
            case 3: // Profile
                intent = new Intent(this, ProfileActivity.class);
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
        int defaultBackground = android.R.color.transparent;
        int selectedBackground = ContextCompat.getColor(this, R.color.gray_light);

        homeLayout.setBackgroundColor(defaultBackground);
        cartLayout.setBackgroundColor(defaultBackground);
        ordersLayout.setBackgroundColor(defaultBackground);
        profileLayout.setBackgroundColor(defaultBackground);

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

    private void updateCartBadge() {
        int itemCount = CartManager.getInstance().getCartItems().size();
        if (itemCount > 0) {
            cartBadge.setText(String.valueOf(itemCount));
            cartBadge.setVisibility(TextView.VISIBLE);
        } else {
            cartBadge.setVisibility(TextView.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge(); // Cập nhật badge khi quay lại Activity
    }
}