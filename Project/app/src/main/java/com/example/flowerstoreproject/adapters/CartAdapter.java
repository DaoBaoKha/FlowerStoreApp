package com.example.flowerstoreproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Product;
import com.example.flowerstoreproject.utils.CartManager;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private List<Product> cartItems;

    public CartAdapter(Context context) {
        this.context = context;
        this.cartItems = CartManager.getInstance().getCartItems();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartItems.get(position);
        holder.tvCartProductName.setText(product.getName());
        holder.tvCartProductPrice.setText("$" + product.getPrice());
        holder.tvCartProductQuantity.setText("Số lượng: 1"); // Hiện tại chỉ hỗ trợ 1 sản phẩm/lần
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems() {
        this.cartItems = CartManager.getInstance().getCartItems();
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvCartProductName, tvCartProductPrice, tvCartProductQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCartProductName = itemView.findViewById(R.id.tvCartProductName);
            tvCartProductPrice = itemView.findViewById(R.id.tvCartProductPrice);
            tvCartProductQuantity = itemView.findViewById(R.id.tvCartProductQuantity);
        }
    }
}