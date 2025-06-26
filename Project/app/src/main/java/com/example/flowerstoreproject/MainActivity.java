package com.example.flowerstoreproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView tvWelcome;
    private Button btnBrowseFlowers, btnViewOrders;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            // Initialize views
            tvWelcome = findViewById(R.id.tvWelcome);
            btnBrowseFlowers = findViewById(R.id.btnBrowseFlowers);
            btnViewOrders = findViewById(R.id.btnViewOrders);

            // Initialize SharedPreferences
            sharedPreferences = getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

            // Set welcome message with user's full name
            String fullName = sharedPreferences.getString("full_name", "Guest");
            Log.d(TAG, "User full name: " + fullName);
            tvWelcome.setText("Welcome, " + fullName + "!");

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
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error loading MainActivity", Toast.LENGTH_SHORT).show();
        }
    }
}