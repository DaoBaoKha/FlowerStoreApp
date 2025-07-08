package com.example.flowerstoreproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orders;
    private OnPayClickListener listener;

    public interface OnPayClickListener {
        void onPayClicked(String orderId);
    }

    public void setOnPayClickListener(OnPayClickListener listener) {
        this.listener = listener;
    }

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
        holder.tvOrderId.setText("Mã đơn: " + order.getId().substring(0, 6) + "...");
        holder.tvOrderAmount.setText("Tổng tiền: $" + order.getTotalAmount());
        holder.tvOrderStatus.setText("Trạng thái: " + order.getStatus());
        holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderAt().substring(0, 10));

        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPayClicked(order.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderAmount, tvOrderStatus, tvOrderDate;
        Button btnPay;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
}
