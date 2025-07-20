package com.example.flowerstoreproject.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.ShipperService;
import com.example.flowerstoreproject.model.Order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipperOrdersFragment extends Fragment {

    private RecyclerView recyclerOrders;
    private TextView tvOrderCount, tvEmptyMessageOrders;
    private ProgressBar progressBarOrders;
    private SwipeRefreshLayout swipeRefreshOrders;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipper_orders, container, false);

        recyclerOrders = view.findViewById(R.id.recycler_orders);
        tvOrderCount = view.findViewById(R.id.tv_order_count);
        tvEmptyMessageOrders = view.findViewById(R.id.tv_empty_message_orders);
        progressBarOrders = view.findViewById(R.id.progress_bar_orders);
        swipeRefreshOrders = view.findViewById(R.id.swipe_refresh_orders);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerOrders.setAdapter(new OrderAdapter());

        sharedPreferences = requireActivity().getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

        progressBarOrders.setVisibility(View.VISIBLE);
        loadAssignedOrders();

        swipeRefreshOrders.setOnRefreshListener(() -> loadAssignedOrders());

        return view;
    }

    private void loadAssignedOrders() {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy token xác thực", Toast.LENGTH_SHORT).show();
            progressBarOrders.setVisibility(View.GONE);
            return;
        }

        ShipperService shipperService = RetrofitClient.getClient().create(ShipperService.class);
        Call<List<Order>> call = shipperService.getMyOrders("Bearer " + token);
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBarOrders.setVisibility(View.GONE);
                swipeRefreshOrders.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    tvOrderCount.setText(orders.size() + " đơn hàng");
                    if (!orders.isEmpty()) {
                        ((OrderAdapter) recyclerOrders.getAdapter()).updateData(orders);
                        tvEmptyMessageOrders.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessageOrders.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptyMessageOrders.setText("Failed to load orders. Error: " + response.code());
                    tvEmptyMessageOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBarOrders.setVisibility(View.GONE);
                swipeRefreshOrders.setRefreshing(false);
                tvEmptyMessageOrders.setText("Error: " + t.getMessage());
                tvEmptyMessageOrders.setVisibility(View.VISIBLE);
            }
        });
    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order> orderList = new ArrayList<>();
        private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public void updateData(List<Order> newData) {
            orderList.clear();
            orderList.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shipper_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            holder.tvOrderId.setText("Mã đơn: " + order.getId());
            holder.tvCustomerName.setText("Khách hàng: " + order.getAccountId().getFullName());
            holder.tvTotalAmount.setText("Tổng tiền: " + order.getTotalAmount() + " VND");
            holder.tvAddress.setText("Địa chỉ: " + order.getAddressShip());

            try {
                Date orderAt = isoFormat.parse(order.getOrderAt());
                holder.tvOrderAt.setText("Đặt lúc: " + displayFormat.format(orderAt));
            } catch (ParseException e) {
                holder.tvOrderAt.setText("Đặt lúc: Không hợp lệ");
            }

            holder.tvStatus.setText("Trạng thái: " + order.getStatus());

            // mở Google Maps
            holder.btnViewMap.setOnClickListener(v -> {
                String address = order.getAddressShip();
                openMapWithAddress(address);
            });
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }

        // ✅ Hàm mở Google Maps với địa chỉ
        private void openMapWithAddress(String address) {
            String mapUri = "http://maps.google.com/maps?q=" + address.replace(" ", "+");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(ShipperOrdersFragment.this.requireActivity().getPackageManager()) != null) {
                ShipperOrdersFragment.this.startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy ứng dụng Google Maps", Toast.LENGTH_SHORT).show();
            }
        }

        public class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvCustomerName, tvTotalAmount, tvAddress, tvOrderAt, tvStatus;
            Button btnViewMap;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tv_order_id);
                tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
                tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
                tvAddress = itemView.findViewById(R.id.tv_address);
                tvOrderAt = itemView.findViewById(R.id.tv_order_at);
                tvStatus = itemView.findViewById(R.id.tv_status);
                btnViewMap = itemView.findViewById(R.id.btn_view_map);
            }
        }
    }
}
