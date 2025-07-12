package com.example.flowerstoreproject.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.AdminOrderAdapter;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.OrderService;
import com.example.flowerstoreproject.model.Order;
import com.example.flowerstoreproject.model.OrderListResponse;
import com.example.flowerstoreproject.model.UpdateOrderStatusRequest;
import com.example.flowerstoreproject.model.UpdateOrderStatusResponse;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrdersFragment extends Fragment {
    private static final String TAG = "AdminOrdersFragment";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private Spinner spinnerFilter;

    private AdminOrderAdapter orderAdapter;
    private List<Order> orderList;
    private OrderService orderService;
    private SharedPreferences sharedPreferences;
    private String authToken;

    private String[] statusOptions = { "Chờ xử lý", "Đã xác nhận", "Đang giao", "Đã giao", "Đã hủy"};
    private String[] statusValues = {"pending", "confirmed", "shipped", "delivered", "cancelled"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        initViews(view);
        setupAuth();
        setupRecyclerView();
        setupSpinner();
        setupSwipeRefresh();

        loadOrders();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_orders);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        spinnerFilter = view.findViewById(R.id.spinner_filter);
    }

    private void setupAuth() {
        sharedPreferences = getActivity().getSharedPreferences("FlowerShopPrefs", getActivity().MODE_PRIVATE);
        authToken = "Bearer " + sharedPreferences.getString("token", "");
        orderService = RetrofitClient.getClient().create(OrderService.class);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new AdminOrderAdapter(orderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(orderAdapter);

        // Set up listeners
        orderAdapter.setOnStatusUpdateListener(this::showUpdateStatusDialog);
        orderAdapter.setOnViewDetailsListener(this::showOrderDetails);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = statusValues[position];
                orderAdapter.filter(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                orderAdapter.filter("");
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadOrders);
    }

    private void loadOrders() {
        Log.d(TAG, "loadOrders: Loading orders from API");
        progressBar.setVisibility(View.VISIBLE);

        Call<OrderListResponse> call = orderService.getOrders(authToken);
        call.enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(Call<OrderListResponse> call, Response<OrderListResponse> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "loadOrders: Successfully loaded " + response.body().getData().size() + " orders");
                    orderList.clear();
                    orderList.addAll(response.body().getData());
                    orderAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "loadOrders: Failed to load orders, response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "loadOrders: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateStatusDialog(Order order) {
        String[] statusLabels = {"Chờ xử lý", "Đã xác nhận", "Đang giao", "Đã giao", "Đã hủy"};
        String[] statusValues = {"pending", "confirmed", "shipped", "delivered", "cancelled"};

        // Find current status index
        int currentIndex = 0;
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equals(order.getStatus())) {
                currentIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Cập nhật trạng thái đơn hàng")
                .setSingleChoiceItems(statusLabels, currentIndex, (dialog, which) -> {
                    String newStatus = statusValues[which];
                    updateOrderStatus(order.getId(), newStatus);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        Log.d(TAG, "updateOrderStatus: Updating order " + orderId + " to status " + newStatus);
        progressBar.setVisibility(View.VISIBLE);

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(newStatus);
        Call<UpdateOrderStatusResponse> call = orderService.updateOrderStatus(authToken, orderId, request);

        call.enqueue(new Callback<UpdateOrderStatusResponse>() {
            @Override
            public void onResponse(Call<UpdateOrderStatusResponse> call, Response<UpdateOrderStatusResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "updateOrderStatus: Successfully updated order status");
                    Toast.makeText(getContext(), "Order status updated successfully", Toast.LENGTH_SHORT).show();
                    loadOrders(); // Reload to get updated data
                } else {
                    Log.e(TAG, "updateOrderStatus: Failed to update order status, response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to update order status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateOrderStatusResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "updateOrderStatus: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOrderDetails(Order order) {
        OrderDetailsDialog dialog = new OrderDetailsDialog(getContext(), order);
        dialog.show();
    }
}