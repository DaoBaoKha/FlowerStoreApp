package com.example.flowerstoreproject.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;

public class CartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        // Thêm logic cho CartActivity (chưa có layout, bạn cần tạo)
        TextView textView = findViewById(R.id.cart_text_view);
        textView.setText("This is Cart Page");
    }
}