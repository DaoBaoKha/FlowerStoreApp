package com.example.flowerstoreproject.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.CartAdapter;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.OrderService;
import com.example.flowerstoreproject.model.CreateOrderResponse;
import com.example.flowerstoreproject.model.OrderItem;
import com.example.flowerstoreproject.model.OrderRequest;
import com.example.flowerstoreproject.model.Product;
import com.example.flowerstoreproject.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private Button btnCheckout;
    private EditText etAddress;
    private LinearLayout homeLayout, cartLayout, ordersLayout, profileLayout;
    private TextView homeText, cartText, ordersText, profileText, cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Khởi tạo views
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        btnCheckout = findViewById(R.id.btnPlaceOrder);
        etAddress = findViewById(R.id.etAddress); // Thêm EditText cho địa chỉ

        // Khởi tạo thanh taskbar
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
        cartAdapter = new CartAdapter(this);
        cartRecyclerView.setAdapter(cartAdapter);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cập nhật badge giỏ hàng
        updateCartBadge();

        // Thiết lập sự kiện click cho nút Checkout
        btnCheckout.setOnClickListener(v -> createOrder());

        // Kiểm tra và kích hoạt nút Checkout dựa trên địa chỉ
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableCheckoutButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        enableCheckoutButton(); // Kiểm tra ban đầu

        // Thiết lập sự kiện click cho thanh taskbar
        homeLayout.setOnClickListener(v -> navigateTo(0));
        cartLayout.setOnClickListener(v -> navigateTo(1));
        ordersLayout.setOnClickListener(v -> navigateTo(2));
        profileLayout.setOnClickListener(v -> navigateTo(3));

        // Đặt Cart là mặc định được chọn
        updateNavigationSelection(1);
    }

    private void enableCheckoutButton() {
        boolean isAddressValid = !etAddress.getText().toString().trim().isEmpty();
        btnCheckout.setEnabled(isAddressValid);
        if (!isAddressValid) {
            btnCheckout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light)); // Màu xám khi vô hiệu
        } else {
            btnCheckout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.category_item_text_normal)); // Màu xanh khi kích hoạt
        }
    }

    private void createOrder() {
        SharedPreferences sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Bạn cần đăng nhập trước khi đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Product> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (Product product : cartItems) {
            orderItems.add(new OrderItem(product.getId(), 1)); // mỗi sản phẩm số lượng 1
        }

        OrderRequest orderRequest = new OrderRequest(
                address, // Sử dụng địa chỉ nhập từ người dùng
                100, // Giá trị tạm thời, cần tính toán thực tế
                orderItems
        );

        OrderService orderService = RetrofitClient.getClient().create(OrderService.class);
        Call<CreateOrderResponse> call = orderService.createOrder("Bearer " + token, orderRequest);

        call.enqueue(new Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CartActivity.this, "Tạo đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    CartManager.getInstance().clearCart();
                    updateCartBadge(); // Cập nhật badge sau khi xóa giỏ hàng
                    finish(); // quay lại màn hình trước
                } else {
                    Toast.makeText(CartActivity.this, "Tạo đơn thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("CartActivity", "Lỗi tạo đơn: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CartActivity", "Lỗi mạng khi tạo đơn", t);
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
                return; // Ở lại trang Cart
            case 2: // Orders
                intent = new Intent(this, OrdersActivity.class);
                break;
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