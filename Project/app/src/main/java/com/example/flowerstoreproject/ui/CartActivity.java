package com.example.flowerstoreproject.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        btnCheckout = findViewById(R.id.btnPlaceOrder);

        cartAdapter = new CartAdapter(this);
        cartRecyclerView.setAdapter(cartAdapter);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnCheckout.setOnClickListener(v -> createOrder());
    }

    private void createOrder() {
        SharedPreferences sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Bạn cần đăng nhập trước khi đặt hàng", Toast.LENGTH_SHORT).show();
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
                "123 Main St, City, Country",
                10.99,
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
}
