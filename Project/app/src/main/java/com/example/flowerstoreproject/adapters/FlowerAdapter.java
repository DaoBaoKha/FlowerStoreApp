package com.example.flowerstoreproject.ui.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Flower;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.FlowerViewHolder> {

    private List<Flower> flowerList;
    private Context context;
    private OnFlowerClickListener listener;

    public interface OnFlowerClickListener {
        void onFlowerClick(Flower flower);
        void onEditClick(Flower flower);
        void onDeleteClick(Flower flower);
    }

    public FlowerAdapter(Context context, List<Flower> flowerList, OnFlowerClickListener listener) {
        this.context = context;
        this.flowerList = flowerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flower_admin, parent, false);
        return new FlowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlowerViewHolder holder, int position) {
        Flower flower = flowerList.get(position);

        holder.tvName.setText(flower.getName());
        holder.tvDescription.setText(flower.getDescription());

        // Format price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        holder.tvPrice.setText(formatter.format(flower.getPrice()));

        // Set stock
        holder.tvStock.setText("Stock: " + flower.getStock());

        // Set category
        if (flower.getCategory() != null) {
            holder.tvCategory.setText(flower.getCategory().getName());
        } else {
            holder.tvCategory.setText("Unknown Category");
        }

        // Load image with Glide
        if (flower.getImage() != null && !flower.getImage().isEmpty()) {
            Glide.with(context)
                    .load(flower.getImage())
                    .placeholder(R.drawable.ic_flower_empty)
                    .into(holder.ivFlower);
        } else {
            holder.ivFlower.setImageResource(R.drawable.ic_flower_empty);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFlowerClick(flower);
            }
        });

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(flower);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(flower);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flowerList.size();
    }

    public void updateFlowers(List<Flower> newFlowers) {
        this.flowerList = newFlowers;
        notifyDataSetChanged();
    }

    public void removeFlower(int position) {
        if (position >= 0 && position < flowerList.size()) {
            flowerList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addFlower(Flower flower) {
        flowerList.add(0, flower);
        notifyItemInserted(0);
    }

    public void updateFlower(int position, Flower flower) {
        if (position >= 0 && position < flowerList.size()) {
            flowerList.set(position, flower);
            notifyItemChanged(position);
        }
    }

    public class FlowerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFlower, ivEdit, ivDelete;
        TextView tvName, tvDescription, tvPrice, tvStock, tvCategory;

        public FlowerViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFlower = itemView.findViewById(R.id.iv_flower);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }
    }
}