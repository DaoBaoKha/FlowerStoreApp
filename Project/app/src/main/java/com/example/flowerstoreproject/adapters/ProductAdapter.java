package com.example.flowerstoreproject.adapters;

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
import com.example.flowerstoreproject.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> products;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        //holder.tvProductId.setText("ID: " + product.getId());
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText("$" + product.getPrice());
        holder.tvProductDescription.setText(product.getDescription());
        holder.tvProductCategory.setText("Danh mục: " + product.getCategory().getName());
        //holder.tvProductStock.setText("Tồn kho: " + product.getStock());
        //holder.tvProductIsActive.setText("Hoạt động: " + (product.isActive() ? "Có" : "Không"));
        //holder.tvProductCreateBy.setText("Tạo bởi: " + product.getCreator().getFullName());

        Glide.with(context)
                .load(product.getImage())
                .into(holder.ivProductImage);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductId, tvProductName, tvProductPrice, tvProductDescription,
                tvProductCategory, tvProductStock, tvProductIsActive, tvProductCreateBy;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            //tvProductId = itemView.findViewById(R.id.tvProductId);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            //tvProductStock = itemView.findViewById(R.id.tvProductStock);
            //tvProductIsActive = itemView.findViewById(R.id.tvProductIsActive);
            //tvProductCreateBy = itemView.findViewById(R.id.tvProductCreateBy);
        }
    }
}