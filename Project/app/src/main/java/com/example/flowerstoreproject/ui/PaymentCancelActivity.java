package com.example.flowerstoreproject.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flowerstoreproject.R;

public class PaymentCancelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_cancel);

        TextView statusText = findViewById(R.id.statusText);
        statusText.setText("Bạn đã hủy thanh toán.");
    }
}
