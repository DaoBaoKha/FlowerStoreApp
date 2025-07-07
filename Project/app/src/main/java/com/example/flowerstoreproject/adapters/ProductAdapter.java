package com.example.flowerstoreproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> products;
    private final OnAddToCartListener listener; // Interface để thông báo khi thêm vào giỏ

    // Interface để thông báo khi nhấn nút "Thêm vào giỏ"
    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    // Cập nhật constructor để nhận OnAddToCartListener
    public ProductAdapter(Context context, List<Product> products, OnAddToCartListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener; // Gán giá trị từ tham số
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

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText("$" + product.getPrice());
        holder.tvProductDescription.setText(product.getDescription());

        Glide.with(context)
                .load(product.getImage())
                .into(holder.ivProductImage);

        // Thiết lập sự kiện nhấp vào nút "Thêm vào giỏ"
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(product);
                Toast.makeText(context, product.getName() + " đã được thêm vào giỏ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductDescription;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart); // Thêm tham chiếu đến Button
        }
    }
}