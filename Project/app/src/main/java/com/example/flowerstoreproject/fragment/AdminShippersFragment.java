package com.example.flowerstoreproject.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.OrderService;
import com.example.flowerstoreproject.api.services.ShipperService;
import com.example.flowerstoreproject.model.Order;
import com.example.flowerstoreproject.model.OrderListResponse;
import com.example.flowerstoreproject.model.OrderAssignRequest;
import com.example.flowerstoreproject.model.Shipper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminShippersFragment extends Fragment {

    private RecyclerView recyclerShippers;
    private TextView tvEmptyMessageShippers;
    private ProgressBar progressBarShippers;
    private SwipeRefreshLayout swipeRefreshShippers;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_shippers, container, false);

        recyclerShippers = view.findViewById(R.id.recycler_shippers);
        tvEmptyMessageShippers = view.findViewById(R.id.tv_empty_message_shippers);
        progressBarShippers = view.findViewById(R.id.progress_bar_shippers);
        swipeRefreshShippers = view.findViewById(R.id.swipe_refresh_shippers);

        recyclerShippers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerShippers.setAdapter(new ShipperAdapter());

        sharedPreferences = requireActivity().getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

        progressBarShippers.setVisibility(View.VISIBLE);

        ShipperService shipperService = RetrofitClient.getClient().create(ShipperService.class);
        Call<List<Shipper>> call = shipperService.getAllShippers();
        call.enqueue(new Callback<List<Shipper>>() {
            @Override
            public void onResponse(Call<List<Shipper>> call, Response<List<Shipper>> response) {
                progressBarShippers.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Shipper> shippers = response.body();
                    if (!shippers.isEmpty()) {
                        ((ShipperAdapter) recyclerShippers.getAdapter()).updateData(shippers);
                        tvEmptyMessageShippers.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptyMessageShippers.setText("Failed to load shippers. Error: " + response.code());
                    tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Shipper>> call, Throwable t) {
                progressBarShippers.setVisibility(View.GONE);
                tvEmptyMessageShippers.setText("Error: " + t.getMessage());
                tvEmptyMessageShippers.setVisibility(View.VISIBLE);
            }
        });

        swipeRefreshShippers.setOnRefreshListener(() -> {
            call.clone().enqueue(new Callback<List<Shipper>>() {
                @Override
                public void onResponse(Call<List<Shipper>> call, Response<List<Shipper>> response) {
                    swipeRefreshShippers.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Shipper> shippers = response.body();
                        if (!shippers.isEmpty()) {
                            ((ShipperAdapter) recyclerShippers.getAdapter()).updateData(shippers);
                            tvEmptyMessageShippers.setVisibility(View.GONE);
                        } else {
                            tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Shipper>> call, Throwable t) {
                    swipeRefreshShippers.setRefreshing(false);
                    tvEmptyMessageShippers.setText("Error: " + t.getMessage());
                    tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                }
            });
        });

        return view;
    }

    private class ShipperAdapter extends RecyclerView.Adapter<ShipperAdapter.ShipperViewHolder> {
        private List<Shipper> shipperList = new ArrayList<>();
        private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public void updateData(List<Shipper> newData) {
            shipperList.clear();
            shipperList.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_shippers, parent, false);
            return new ShipperViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ShipperViewHolder holder, int position) {
            Shipper shipper = shipperList.get(position);
            holder.tvFullName.setText("Tên: " + shipper.getFullName());
            holder.tvEmail.setText("Email: " + shipper.getEmail());
            holder.tvPhone.setText("Điện thoại: " + shipper.getPhone());
            holder.tvRole.setText("Vai trò: " + shipper.getRole());
            holder.tvIsActive.setText("Hoạt động: " + (shipper.isActive() ? "Có" : "Không"));

            // Chuyển đổi createdAt từ String sang Date và định dạng
            try {
                Date createdAt = isoFormat.parse(shipper.getCreatedAt());
                holder.tvCreatedAt.setText("Tạo lúc: " + displayFormat.format(createdAt));
            } catch (ParseException e) {
                holder.tvCreatedAt.setText("Tạo lúc: Không hợp lệ");
            }

            // Chuyển đổi updatedAt từ String sang Date và định dạng
            try {
                Date updatedAt = isoFormat.parse(shipper.getUpdatedAt());
                holder.tvUpdatedAt.setText("Cập nhật lúc: " + displayFormat.format(updatedAt));
            } catch (ParseException e) {
                holder.tvUpdatedAt.setText("Cập nhật lúc: Không hợp lệ");
            }

            holder.tvVersion.setText("Phiên bản: " + shipper.getVersion());

            Glide.with(getContext())
                    .load(shipper.getAvatar())
                    .placeholder(R.drawable.ic_flower_empty)
                    .error(R.drawable.ic_info)
                    .into(holder.ivAvatar);

            holder.btnAssignOrder.setOnClickListener(v -> loadPaidOrders(shipper));
        }

        private void loadPaidOrders(Shipper shipper) {
            String token = sharedPreferences.getString("token", "");
            if (token.isEmpty()) {
                Toast.makeText(getContext(), "Không tìm thấy token xác thực", Toast.LENGTH_SHORT).show();
                return;
            }

            OrderService orderService = RetrofitClient.getClient().create(OrderService.class);
            Call<OrderListResponse> call = orderService.getOrders("Bearer " + token);
            call.enqueue(new Callback<OrderListResponse>() {
                @Override
                public void onResponse(Call<OrderListResponse> call, Response<OrderListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> orders = response.body().getData();
                        List<Order> paidOrders = new ArrayList<>();
                        for (Order order : orders) {
                            if ("paid".equals(order.getStatus())) {
                                paidOrders.add(order);
                            }
                        }
                        if (!paidOrders.isEmpty()) {
                            showOrderSelectionDialog(shipper, paidOrders);
                        } else {
                            Toast.makeText(getContext(), "Không có đơn hàng nào có trạng thái 'paid'", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lấy danh sách đơn hàng thất bại. Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<OrderListResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void showOrderSelectionDialog(Shipper shipper, List<Order> paidOrders) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Chọn Đơn Hàng để Gán");

            OrderSelectionAdapter adapter = new OrderSelectionAdapter(getContext(), paidOrders);
            builder.setAdapter(adapter, (dialog, which) -> {
                Order selectedOrder = paidOrders.get(which);
                assignOrderToShipper(shipper.getId(), selectedOrder.getId());
            });

            builder.setNegativeButton("Hủy", null);
            builder.show();
        }

        private void assignOrderToShipper(String shipperId, String orderId) {
            ShipperService shipperService = RetrofitClient.getClient().create(ShipperService.class);
            Call<Object> call = shipperService.assignOrder(orderId, new OrderAssignRequest(shipperId));
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Gán đơn hàng " + orderId + " cho shipper " + shipperId + " thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Gán đơn hàng thất bại. Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return shipperList.size();
        }

        public class ShipperViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvFullName, tvEmail, tvPhone, tvRole, tvIsActive, tvCreatedAt, tvUpdatedAt, tvVersion;
            Button btnAssignOrder;

            public ShipperViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.ivAvatar);
                tvFullName = itemView.findViewById(R.id.tvFullName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvRole = itemView.findViewById(R.id.tvRole);
                tvIsActive = itemView.findViewById(R.id.tvIsActive);
                tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
                tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
                tvVersion = itemView.findViewById(R.id.tvVersion);
                btnAssignOrder = itemView.findViewById(R.id.btnAssignOrder);
            }
        }
    }

    // Custom Adapter cho dialog chọn đơn hàng
    private static class OrderSelectionAdapter extends ArrayAdapter<Order> {
        private final List<Order> orders;
        public OrderSelectionAdapter(Context context, List<Order> orders) {
            super(context, 0, orders);
            this.orders = orders;
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Order order = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_order_selection, parent, false);
            }
            TextView tvOrderId = convertView.findViewById(R.id.tvOrderId);
            TextView tvOrderCustomer = convertView.findViewById(R.id.tvOrderCustomer);
            TextView tvOrderAmount = convertView.findViewById(R.id.tvOrderAmount);
            tvOrderId.setText("Mã: " + order.getId().substring(0, 8) + "...");
            tvOrderCustomer.setText("Khách: " + (order.getAccountId() != null ? order.getAccountId().getFullName() : ""));
            tvOrderAmount.setText("Tổng: " + String.format("%,.0f", order.getTotalAmount()) + " VND");
            return convertView;
        }
    }
}