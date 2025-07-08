package com.example.flowerstoreproject.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.OrderAdapter;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.OrderService;
import com.example.flowerstoreproject.model.Order;
import com.example.flowerstoreproject.model.OrderListResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderAdapter = new OrderAdapter(orders);
        ordersRecyclerView.setAdapter(orderAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        OrderService orderService = RetrofitClient.getClient().create(OrderService.class);
        orderService.getOrders("Bearer " + token).enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(Call<OrderListResponse> call, Response<OrderListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Order> orders = response.body().getData();
                    orderAdapter = new OrderAdapter(orders);
                    ordersRecyclerView.setAdapter(orderAdapter);
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
}
