package com.example.flowerstoreproject.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;

public class ShipperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper);

        // Khởi tạo views
        TextView tvTitle = findViewById(R.id.tvTitle);
        Button btnOpenMaps = findViewById(R.id.btnOpenMaps);

        // Thiết lập tiêu đề
        tvTitle.setText("Shipper Dashboard");

        // Thiết lập nút Open Google Maps (placeholder)
        btnOpenMaps.setOnClickListener(v -> {
            // Placeholder, sẽ tích hợp Google Maps sau
        });
    }
}