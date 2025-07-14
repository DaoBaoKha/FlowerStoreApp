package com.example.flowerstoreproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categories;
    private final OnCategoryClickListener listener;
    private String activeCategoryId;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
        this.activeCategoryId = null;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textView.setText(category.getName());
        holder.itemView.setSelected(activeCategoryId != null && activeCategoryId.equals(category.getId()));

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                String previousActiveId = activeCategoryId;
                activeCategoryId = category.getId();
                if (!activeCategoryId.equals(previousActiveId)) {
                    listener.onCategoryClick(category);
                    notifyDataSetChanged(); // Làm mới toàn bộ danh sách
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setActiveCategory(String categoryId) {
        this.activeCategoryId = categoryId;
        notifyDataSetChanged(); // Làm mới giao diện khi active category thay đổi
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}