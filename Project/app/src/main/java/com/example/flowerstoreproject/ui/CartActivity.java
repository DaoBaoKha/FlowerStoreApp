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
import com.example.flowerstoreproject.model.CartItem;
import com.example.flowerstoreproject.model.CreateOrderResponse;
import com.example.flowerstoreproject.model.OrderItem;
import com.example.flowerstoreproject.model.OrderRequest;
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

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        btnCheckout = findViewById(R.id.btnPlaceOrder);
        etAddress = findViewById(R.id.etAddress);

        homeLayout = findViewById(R.id.home_layout);
        cartLayout = findViewById(R.id.cart_layout);
        ordersLayout = findViewById(R.id.orders_layout);
        profileLayout = findViewById(R.id.profile_layout);
        homeText = findViewById(R.id.home_text);
        cartText = findViewById(R.id.cart_text);
        ordersText = findViewById(R.id.orders_text);
        profileText = findViewById(R.id.profile_text);
        cartBadge = findViewById(R.id.cart_badge);

        cartAdapter = new CartAdapter(this);
        cartRecyclerView.setAdapter(cartAdapter);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateCartBadge();

        btnCheckout.setOnClickListener(v -> createOrder());

        etAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableCheckoutButton();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        enableCheckoutButton();

        homeLayout.setOnClickListener(v -> navigateTo(0));
        cartLayout.setOnClickListener(v -> navigateTo(1));
        ordersLayout.setOnClickListener(v -> navigateTo(2));
        profileLayout.setOnClickListener(v -> navigateTo(3));

        updateNavigationSelection(1);
    }

    private void enableCheckoutButton() {
        boolean isAddressValid = !etAddress.getText().toString().trim().isEmpty();
        btnCheckout.setEnabled(isAddressValid);
        btnCheckout.setBackgroundTintList(ContextCompat.getColorStateList(this,
                isAddressValid ? R.color.category_item_text_normal : R.color.gray_light));
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

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;

        for (CartItem cartItem : cartItems) {
            int quantity = cartItem.getQuantity();
            int price = cartItem.getProduct().getPrice();
            totalAmount += price * quantity;

            orderItems.add(new OrderItem(cartItem.getProduct().getId(), quantity));
        }

        OrderRequest orderRequest = new OrderRequest(address, totalAmount, orderItems);

        OrderService orderService = RetrofitClient.getClient().create(OrderService.class);
        Call<CreateOrderResponse> call = orderService.createOrder("Bearer " + token, orderRequest);

        call.enqueue(new Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CartActivity.this, "Tạo đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    CartManager.getInstance().clearCart();
                    updateCartBadge();
                    finish();
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
            case 0: intent = new Intent(this, MainActivity.class); break;
            case 1: return;
            case 2: intent = new Intent(this, OrdersActivity.class); break;
            case 3: intent = new Intent(this, ProfileActivity.class); break;
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
            case 0: homeLayout.setBackgroundColor(selectedBackground); break;
            case 1: cartLayout.setBackgroundColor(selectedBackground); break;
            case 2: ordersLayout.setBackgroundColor(selectedBackground); break;
            case 3: profileLayout.setBackgroundColor(selectedBackground); break;
        }
    }

    private void updateCartBadge() {
        int totalCount = CartManager.getInstance().getTotalItemCount();
        if (totalCount > 0) {
            cartBadge.setText(String.valueOf(totalCount));
            cartBadge.setVisibility(TextView.VISIBLE);
        } else {
            cartBadge.setVisibility(TextView.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartAdapter.notifyDataSetChanged();
        updateCartBadge();
    }
}
