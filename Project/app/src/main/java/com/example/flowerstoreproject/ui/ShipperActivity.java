package com.example.flowerstoreproject.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.fragment.ShipperOrdersFragment;

public class ShipperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper);

        // Khởi tạo views
        Button btnOpenMaps = findViewById(R.id.btnOpenMaps);

        // fragment ShipperOrdersFragment
        loadShipperOrdersFragment();

        // Open Google Maps
        btnOpenMaps.setOnClickListener(v -> {
            // Placeholder, tích hợp Google Maps sau khi chọn đơn hàng
            // Mở bản đồ với địa chỉ từ đơn hàng được chọn
        });
    }

    private void loadShipperOrdersFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new ShipperOrdersFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }
}