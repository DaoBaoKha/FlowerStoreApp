package com.example.flowerstoreproject.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;

public class OrdersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        // Thêm logic cho OrdersActivity (chưa có layout, bạn cần tạo)
        TextView textView = findViewById(R.id.orders_text_view);
        textView.setText("This is Orders Page");
    }
}