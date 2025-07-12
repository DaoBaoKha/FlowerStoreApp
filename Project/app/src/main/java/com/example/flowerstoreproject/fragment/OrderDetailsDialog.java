package com.example.flowerstoreproject.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.OrderItemAdapter;
import com.example.flowerstoreproject.model.Order;

public class OrderDetailsDialog extends Dialog {
    private final Order order;
    private TextView tvOrderId, tvCustomerName, tvCustomerEmail, tvCustomerPhone;
    private TextView tvOrderDate, tvTotalAmount, tvShippingFee, tvStatus;
    private TextView tvShippingAddress, tvTransactionId, tvPaymentCode;
    private RecyclerView recyclerViewItems;
    private OrderItemAdapter itemAdapter;

    public OrderDetailsDialog(@NonNull Context context, Order order) {
        super(context);
        this.order = order;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_order_details);

        initViews();
        setupRecyclerView();
        populateOrderDetails();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tv_order_id);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerEmail = findViewById(R.id.tv_customer_email);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvStatus = findViewById(R.id.tv_status);
        tvShippingAddress = findViewById(R.id.tv_shipping_address);
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        tvPaymentCode = findViewById(R.id.tv_payment_code);
        recyclerViewItems = findViewById(R.id.recycler_view_items);

        findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
    }

    private void setupRecyclerView() {
        itemAdapter = new OrderItemAdapter(order.getItems());
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewItems.setAdapter(itemAdapter);
    }

    private void populateOrderDetails() {
        tvOrderId.setText("Mã đơn: " + order.getId());
        tvCustomerName.setText("Tên khách hàng: " + order.getCustomerName());
        tvCustomerEmail.setText("Email: " + order.getCustomerEmail());
        tvCustomerPhone.setText("Số điện thoại: " + order.getCustomerPhone());
        tvOrderDate.setText("Ngày đặt: " + (order.getOrderAt() != null ? order.getOrderAt().substring(0, 10) : "N/A"));
        tvTotalAmount.setText("Tổng tiền: $" + String.format("%.2f", order.getTotalAmount()));
        tvShippingFee.setText("Phí ship: $" + String.format("%.2f", order.getShippingFee()));
        tvStatus.setText("Trạng thái: " + getStatusLabel(order.getStatus()));
        tvShippingAddress.setText("Địa chỉ giao hàng: " + order.getAddressShip());
        tvTransactionId.setText("Mã giao dịch: " + (order.getTransactionId() != null ? order.getTransactionId() : "N/A"));
        tvPaymentCode.setText("Mã thanh toán: " + (order.getPaymentCode() != null ? order.getPaymentCode() : "N/A"));
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "pending": return "Chờ xử lý";
            case "confirmed": return "Đã xác nhận";
            case "shipped": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }
}