package com.example.flowerstoreproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Rút gọn ID chỉ còn 6 ký tự cuối
        String shortId = order.getId();
        if (shortId.length() > 6) {
            shortId = "..." + shortId.substring(shortId.length() - 6);
        }

        holder.tvOrderId.setText("Mã đơn: " + shortId);
        holder.tvOrderAmount.setText("Tổng tiền: $" + order.getTotalAmount());
        holder.tvOrderStatus.setText("Trạng thái: " + order.getStatus());
        holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderAt().substring(0, 10));
    }


    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderAmount, tvOrderStatus, tvOrderDate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
        }
    }
}
