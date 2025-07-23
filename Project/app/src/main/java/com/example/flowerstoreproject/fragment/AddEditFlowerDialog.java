package com.example.flowerstoreproject.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.CategoryService;
import com.example.flowerstoreproject.model.Category;
import com.example.flowerstoreproject.model.Flower;
import com.example.flowerstoreproject.model.FlowerRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditFlowerDialog  {
    private static final String TAG = "AddEditFlowerDialog";

    private Context context;
    private AlertDialog dialog;
    private Flower flower; // null for add mode, non-null for edit mode
    private OnFlowerSaveListener listener;

    // UI Components
    private EditText etName;
    private EditText etDescription;
    private EditText etPrice;
    private EditText etStock;
    private EditText etImageUrl;
    private Spinner spinnerCategory;
    private ImageView ivPreview;
    private Button btnLoadImage;
    private Button btnSave;
    private Button btnCancel;
    private ProgressBar progressBar;

    // Data
    private List<Category> categories;
    private ArrayAdapter<Category> categoryAdapter;
    private CategoryService categoryService;

    public interface OnFlowerSaveListener {
        void onFlowerSaved(FlowerRequest flowerRequest);
    }

    public AddEditFlowerDialog(Context context, Flower flower, OnFlowerSaveListener listener) {
        this.context = context;
        this.flower = flower;
        this.listener = listener;
        this.categories = new ArrayList<>();
        this.categoryService = RetrofitClient.getClient().create(CategoryService.class);

        initDialog();
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_flower, null);

        initViews(view);
        setupCategorySpinner();
        setupImagePreview();
        setupButtons();
        loadCategories();

        if (flower != null) {
            populateFields();
        }

        builder.setView(view);
        builder.setCancelable(true);

        dialog = builder.create();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.flower_name);
        etDescription = view.findViewById(R.id.flower_description);
        etPrice = view.findViewById(R.id.flower_price);
        etStock = view.findViewById(R.id.flower_stock);
        etImageUrl = view.findViewById(R.id.et_flower_image_url);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        ivPreview = view.findViewById(R.id.iv_flower_preview);
        btnLoadImage = view.findViewById(R.id.btn_load_image);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<Category>(context, android.R.layout.simple_spinner_item, categories) ;
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupImagePreview() {
        btnLoadImage.setOnClickListener(v -> loadImagePreview());

        etImageUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                loadImagePreview();
            }
        });
    }

    private void loadImagePreview() {
        String imageUrl = etImageUrl.getText().toString().trim();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_flower_empty)

                    .into(ivPreview);
        } else {
            ivPreview.setImageResource(R.drawable.ic_flower_empty);
        }
    }

    private void setupButtons() {
        btnSave.setText(flower == null ? "Add Flower" : "Update Flower");

        btnSave.setOnClickListener(v -> saveFlower());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadCategories() {
        Log.d(TAG, "Loading categories");
        progressBar.setVisibility(View.VISIBLE);

        Call<List<Category>> call = categoryService.getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully loaded " + response.body().size() + " categories");
                    categories.clear();
                    // Lọc chỉ lấy category active
                    for (Category c : response.body()) {
                        if (c.isActive()) categories.add(c);
                    }
                    categoryAdapter.notifyDataSetChanged();

                    // Set selected category if editing
                    if (flower != null && flower.getCategory() != null) {
                        setSelectedCategory(flower.getCategory().getId());
                    }
                } else {
                    Log.e(TAG, "Failed to load categories, response code: " + response.code());
                    Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error loading categories", t);
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSelectedCategory(String categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == categoryId) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void populateFields() {
        etName.setText(flower.getName());
        etDescription.setText(flower.getDescription());
        etPrice.setText(String.valueOf(flower.getPrice()));
        etStock.setText(String.valueOf(flower.getStock()));
        etImageUrl.setText(flower.getImage());

        // Load image preview
        loadImagePreview();
    }

    private void saveFlower() {
        if (!validateFields()) {
            return;
        }

        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        int stock = Integer.parseInt(etStock.getText().toString().trim());
        String imageUrl = etImageUrl.getText().toString().trim();

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        if (selectedCategory == null) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        FlowerRequest flowerRequest = new FlowerRequest(
                name,
                price,
                description,
                imageUrl,
                selectedCategory.getId(),
                stock
        );

        if (listener != null) {
            listener.onFlowerSaved(flowerRequest);
        }

        dialog.dismiss();
    }

    private boolean validateFields() {
        // Validate name
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        // Validate description
        if (TextUtils.isEmpty(etDescription.getText().toString().trim())) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        // Validate price
        String priceStr = etPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0");
                etPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return false;
        }

        // Validate stock
        String stockStr = etStock.getText().toString().trim();
        if (TextUtils.isEmpty(stockStr)) {
            etStock.setError("Stock is required");
            etStock.requestFocus();
            return false;
        }

        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                etStock.setError("Stock cannot be negative");
                etStock.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etStock.setError("Invalid stock format");
            etStock.requestFocus();
            return false;
        }

        // Validate image URL
        if (TextUtils.isEmpty(etImageUrl.getText().toString().trim())) {
            etImageUrl.setError("Image URL is required");
            etImageUrl.requestFocus();
            return false;
        }

        // Validate category selection
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}