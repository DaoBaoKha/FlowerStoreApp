package com.example.flowerstoreproject.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.AdminCategoryAdapter;

import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.CategoryService;
import com.example.flowerstoreproject.api.services.FlowerService;
import com.example.flowerstoreproject.model.ApiResponse;
import com.example.flowerstoreproject.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoriesFragment extends Fragment implements AdminCategoryAdapter.OnCategoryActionListener {
    private static final String TAG = "AdminCategoriesFragment";

    private RecyclerView recyclerView;
    private AdminCategoryAdapter adapter;
    private List<Category> categories;
    private CategoryService categoryService;
    private SharedPreferences sharedPreferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_categories, container, false);

        initViews(view);
        initServices();
        setupRecyclerView();
        setupListeners();
        loadCategories();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_categories);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        fabAddCategory = view.findViewById(R.id.fab_add_category);
    }

    private void initServices() {
        categoryService = RetrofitClient.getClient().create(CategoryService.class);
        sharedPreferences = getActivity().getSharedPreferences("FlowerShopPrefs", getContext().MODE_PRIVATE);
    }

    private void setupRecyclerView() {
        categories = new ArrayList<>();
        adapter = new AdminCategoryAdapter(getContext(), categories, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadCategories);
        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }

        categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "loadCategories: Loaded " + categories.size() + " categories");
                } else {
                    Log.e(TAG, "loadCategories: Error response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "loadCategories: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);

        EditText editCategoryName = dialogView.findViewById(R.id.edit_category_name);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String categoryName = editCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                addCategory(categoryName);
                dialog.dismiss();
            } else {
                editCategoryName.setError("Category name is required");
            }
        });

        dialog.show();
    }

    private void addCategory(String categoryName) {
        Category newCategory = new Category();
        newCategory.setName(categoryName);

        String token = "Bearer " + sharedPreferences.getString("token", "");

        categoryService.createCategory(token, newCategory).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Category>> call, @NonNull Response<ApiResponse<Category>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category added successfully", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Refresh list
                    Log.d(TAG, "addCategory: Category added successfully");
                } else {
                    Log.e(TAG, "addCategory: Error response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to add category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Category>> call, @NonNull Throwable t) {
                Log.e(TAG, "addCategory: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditCategory(Category category) {
        showEditCategoryDialog(category);
    }

    private void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_category, null);

        EditText editCategoryName = dialogView.findViewById(R.id.edit_category_name);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        editCategoryName.setText(category.getName());

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String categoryName = editCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                updateCategory(category.getId(), categoryName, category.isActive());
                dialog.dismiss();
            } else {
                editCategoryName.setError("Category name is required");
            }
        });

        dialog.show();
    }

    private void updateCategory(String categoryId, String categoryName, boolean isActive) {
        Category updatedCategory = new Category();
        updatedCategory.setName(categoryName);
        updatedCategory.setActive(isActive);

        String token = "Bearer " + sharedPreferences.getString("token", "");

        categoryService.updateCategory(token, categoryId, updatedCategory).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Category>> call, @NonNull Response<ApiResponse<Category>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category updated successfully", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Refresh list
                    Log.d(TAG, "updateCategory: Category updated successfully");
                } else {
                    Log.e(TAG, "updateCategory: Error response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to update category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Category>> call, @NonNull Throwable t) {
                Log.e(TAG, "updateCategory: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteCategory(Category category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(String categoryId) {
        String token = "Bearer " + sharedPreferences.getString("token", "");

        categoryService.deleteCategory(token, categoryId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Refresh list
                    Log.d(TAG, "deleteCategory: Category deleted successfully");
                } else {
                    Log.e(TAG, "deleteCategory: Error response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "deleteCategory: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onToggleStatus(Category category) {
        updateCategory(category.getId(), category.getName(), !category.isActive());
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}