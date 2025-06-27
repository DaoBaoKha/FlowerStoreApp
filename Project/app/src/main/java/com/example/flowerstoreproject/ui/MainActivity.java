package com.example.flowerstoreproject.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.services.CategoryService;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.model.Category;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView tvWelcome;
    private Button btnBrowseFlowers, btnViewOrders;
    private ListView categoryListView;
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> adapter;
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            // Initialize views
            tvWelcome = findViewById(R.id.tvWelcome);
            btnBrowseFlowers = findViewById(R.id.btnBrowseFlowers);
            btnViewOrders = findViewById(R.id.btnViewOrders);
            categoryListView = findViewById(R.id.categoryListView);

            // Initialize SharedPreferences
            sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

            // Set welcome message with user's full name
            String fullName = sharedPreferences.getString("full_name", "Guest");
            Log.d(TAG, "User full name: " + fullName);
            tvWelcome.setText("Welcome, " + fullName + "!");

            // Initialize ListView adapter
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
            categoryListView.setAdapter(adapter);

            // Set item click listener for categoryListView
            categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Category selectedCategory = categories.get(position);
                    String categoryId = selectedCategory.getId();
                    Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                    intent.putExtra("categoryId", categoryId);
                    startActivity(intent);
                }
            });

            // Set button click listeners
            btnBrowseFlowers.setOnClickListener(v -> {
                Log.d(TAG, "Browse Flowers clicked");
                // TODO: Implement navigation to flower browsing screen
                // Intent intent = new Intent(MainActivity.this, BrowseFlowersActivity.class);
                // startActivity(intent);
            });

            btnViewOrders.setOnClickListener(v -> {
                Log.d(TAG, "View Orders clicked");
                // TODO: Implement navigation to orders screen
                // Intent intent = new Intent(MainActivity.this, ViewOrdersActivity.class);
                // startActivity(intent);
            });

            // Load categories
            loadCategories();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error loading MainActivity", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCategories() {
        // Sử dụng getClient() để tạo CategoryService
        CategoryService categoryService = RetrofitClient.getClient().create(CategoryService.class);
        Call<List<Category>> call = categoryService.getCategories();

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    updateCategoryList();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading categories", t);
            }
        });
    }

    private void updateCategoryList() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        adapter.clear();
        adapter.addAll(categoryNames);
        adapter.notifyDataSetChanged();
    }
}