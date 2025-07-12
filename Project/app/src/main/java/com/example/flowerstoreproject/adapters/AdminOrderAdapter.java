package com.example.flowerstoreproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Order;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private List<Order> filteredOrders;
    private OnStatusUpdateListener statusUpdateListener;
    private OnViewDetailsListener viewDetailsListener;

    public interface OnStatusUpdateListener {
        void onStatusUpdate(Order order);
    }

    public interface OnViewDetailsListener {
        void onViewDetails(Order order);
    }

    public AdminOrderAdapter(List<Order> orders) {
        this.orders = orders;
        this.filteredOrders = new ArrayList<>(orders);
    }

    public void setOnStatusUpdateListener(OnStatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }

    public void setOnViewDetailsListener(OnViewDetailsListener listener) {
        this.viewDetailsListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = filteredOrders.get(position);

        // Set order information
        holder.tvOrderId.setText("Mã đơn: " + order.getId().substring(0, 6) + "...");
        holder.tvOrderAmount.setText("Tổng tiền: $" + String.format("%.2f", order.getTotalAmount()));
        holder.tvShippingFee.setText("Phí ship: $" + String.format("%.2f", order.getShippingFee()));
        holder.tvOrderAddress.setText("Địa chỉ: " + order.getAddressShip());
        holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderAt().substring(0, 10));

        // Set status with color
        setStatusWithColor(holder.tvOrderStatus, order.getStatus());

        // Set card background based on status
        setCardBackground(holder.cardView, order.getStatus());

        // Set click listeners
        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (statusUpdateListener != null) {
                statusUpdateListener.onStatusUpdate(order);
            }
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (viewDetailsListener != null) {
                viewDetailsListener.onViewDetails(order);
            }
        });

        // Set card click listener for details
        holder.cardView.setOnClickListener(v -> {
            if (viewDetailsListener != null) {
                viewDetailsListener.onViewDetails(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredOrders.size();
    }

    public void filter(String status) {
        filteredOrders.clear();

        if (status.isEmpty()) {
            filteredOrders.addAll(orders);
        } else {
            for (Order order : orders) {
                if (order.getStatus().equalsIgnoreCase(status)) {
                    filteredOrders.add(order);
                }
            }
        }

        notifyDataSetChanged();
    }

    private void setStatusWithColor(TextView textView, String status) {
        String statusLabel = getStatusLabel(status);
        textView.setText("Trạng thái: " + statusLabel);

        int color;
        switch (status) {
            case "pending":
                color = Color.parseColor("#FF9800"); // Orange
                break;
            case "confirmed":
                color = Color.parseColor("#2196F3"); // Blue
                break;
            case "shipped":
                color = Color.parseColor("#9C27B0"); // Purple
                break;
            case "delivered":
                color = Color.parseColor("#4CAF50"); // Green
                break;
            case "cancelled":
                color = Color.parseColor("#F44336"); // Red
                break;
            default:
                color = Color.parseColor("#757575"); // Gray
                break;
        }

        textView.setTextColor(color);
    }

    private void setCardBackground(CardView cardView, String status) {
        int backgroundColor;
        switch (status) {
            case "pending":
                backgroundColor = Color.parseColor("#FFF8E1"); // Light orange
                break;
            case "confirmed":
                backgroundColor = Color.parseColor("#E3F2FD"); // Light blue
                break;
            case "shipped":
                backgroundColor = Color.parseColor("#F3E5F5"); // Light purple
                break;
            case "delivered":
                backgroundColor = Color.parseColor("#E8F5E8"); // Light green
                break;
            case "cancelled":
                backgroundColor = Color.parseColor("#FFEBEE"); // Light red
                break;
            default:
                backgroundColor = Color.parseColor("#FAFAFA"); // Light gray
                break;
        }

        cardView.setCardBackgroundColor(backgroundColor);
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "pending": return "Chờ xử lý";
            case "confirmed": return "Đã xác nhận";
            case "shipped": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvOrderId, tvOrderAmount, tvShippingFee, tvOrderAddress, tvOrderDate, tvOrderStatus;
        Button btnUpdateStatus, btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvShippingFee = itemView.findViewById(R.id.tvShippingFee);
            tvOrderAddress = itemView.findViewById(R.id.tvOrderAddress);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}