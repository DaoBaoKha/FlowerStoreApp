package com.example.flowerstoreproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.OrderItemDetail;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<OrderItemDetail> items;

    public OrderItemAdapter(List<OrderItemDetail> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItemDetail item = items.get(position);

        holder.tvItemName.setText(item.getFlowerId().getName());
        holder.tvItemQuantity.setText("Số lượng: " + item.getQuantity());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvItemPrice.setText("Giá: " + formatter.format(item.getFlowerId().getPrice()));
        holder.tvItemTotal.setText("Tổng: " + formatter.format(item.getFlowerId().getPrice() * item.getQuantity()));

        // Load image using Glide
        if (item.getFlowerId().getImage() != null && !item.getFlowerId().getImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getFlowerId().getImage())
                    .placeholder(R.drawable.ic_info)

                    .into(holder.ivItemImage);
        } else {
            holder.ivItemImage.setImageResource(R.drawable.ic_flower_empty);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvItemName, tvItemQuantity, tvItemPrice, tvItemTotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemQuantity = itemView.findViewById(R.id.tv_item_quantity);
            tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            tvItemTotal = itemView.findViewById(R.id.tv_item_total);
        }
    }
}