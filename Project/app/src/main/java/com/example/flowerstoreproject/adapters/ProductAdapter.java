package com.example.flowerstoreproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Product;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context context;

    public ProductAdapter(Context context, int resource, List<Product> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);
        }

        Product product = getItem(position);

        // Ánh xạ các view từ layout
        ImageView ivProductImage = listItem.findViewById(R.id.ivProductImage);
        TextView tvProductId = listItem.findViewById(R.id.tvProductId);
        TextView tvProductName = listItem.findViewById(R.id.tvProductName);
        TextView tvProductPrice = listItem.findViewById(R.id.tvProductPrice);
        TextView tvProductDescription = listItem.findViewById(R.id.tvProductDescription);
        TextView tvProductCategory = listItem.findViewById(R.id.tvProductCategory);
        TextView tvProductStock = listItem.findViewById(R.id.tvProductStock);
        TextView tvProductIsActive = listItem.findViewById(R.id.tvProductIsActive);
        TextView tvProductCreateBy = listItem.findViewById(R.id.tvProductCreateBy);

        // Gán dữ liệu vào các view
        tvProductId.setText("ID: " + product.getId());
        tvProductName.setText(product.getName());
        tvProductPrice.setText("$" + product.getPrice());
        tvProductDescription.setText(product.getDescription());
        tvProductCategory.setText("Category: " + product.getCategory().getName());
        tvProductStock.setText("Stock: " + product.getStock());
        tvProductIsActive.setText("Active: " + (product.isActive() ? "Yes" : "No"));
        tvProductCreateBy.setText("Created by: " + product.getCreator().getFullName());

        // Tải hình ảnh bằng Glide
        Glide.with(context)
                .load(product.getImage())
                .into(ivProductImage);

        return listItem;
    }
}