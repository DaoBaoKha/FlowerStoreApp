package com.example.flowerstoreproject.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.CategoryAdapter;
import com.example.flowerstoreproject.adapters.ProductAdapter;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.CategoryService;
import com.example.flowerstoreproject.api.services.ProductService;
import com.example.flowerstoreproject.model.Category;
import com.example.flowerstoreproject.model.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView categoryRecyclerView, productRecyclerView;
    private Button btnShowAll;
    private SharedPreferences sharedPreferences;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private List<Category> categories = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            // Khởi tạo views
            categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
            productRecyclerView = findViewById(R.id.productRecyclerView);
            btnShowAll = findViewById(R.id.btnShowAll);

            // Khởi tạo SharedPreferences
            sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

            // Khởi tạo RecyclerView adapters
            categoryAdapter = new CategoryAdapter(this, categories, category -> {
                loadProducts(category.getId()); // Tải sản phẩm theo danh mục
            });
            categoryRecyclerView.setAdapter(categoryAdapter);
            categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            productAdapter = new ProductAdapter(this, products);
            productRecyclerView.setAdapter(productAdapter);
            productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Thiết lập sự kiện click cho nút "Tất cả"
            btnShowAll.setOnClickListener(v -> loadAllProducts());

            // Tải danh mục và tất cả sản phẩm khi khởi động
            loadCategories();
            loadAllProducts();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onCreate: ", e);
            Toast.makeText(this, "Lỗi tải MainActivity", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCategories() {
        CategoryService categoryService = RetrofitClient.getClient().create(CategoryService.class);
        Call<List<Category>> call = categoryService.getCategories();

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    categoryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Không tải được danh mục", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi tải danh mục", t);
            }
        });
    }

    private void loadProducts(String categoryId) {
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        Call<List<Product>> call = productService.getProductsByCategory(categoryId);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    products.clear();
                    products.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi tải sản phẩm", t);
            }
        });
    }

    private void loadAllProducts() {
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        Call<List<Product>> call = productService.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    products.clear();
                    products.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Không tải được tất cả sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi tải tất cả sản phẩm", t);
            }
        });
    }
}