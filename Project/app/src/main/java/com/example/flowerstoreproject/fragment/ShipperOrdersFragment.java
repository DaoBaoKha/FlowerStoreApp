package com.example.flowerstoreproject.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.ShipperService;
import com.example.flowerstoreproject.model.Order;
import com.example.flowerstoreproject.ui.MapActivity;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipperOrdersFragment extends Fragment {

    private RecyclerView recyclerOrders;
    private TextView tvOrderCount, tvEmptyMessageOrders;
    private ProgressBar progressBarOrders;
    private SwipeRefreshLayout swipeRefreshOrders;
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private String currentOrderId;
    private static final String TAG = "ShipperOrdersFragment";
    private Bitmap capturedBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipper_orders, container, false);

        recyclerOrders = view.findViewById(R.id.recycler_orders);
        tvOrderCount = view.findViewById(R.id.tv_order_count);
        tvEmptyMessageOrders = view.findViewById(R.id.tv_empty_message_orders);
        progressBarOrders = view.findViewById(R.id.progress_bar_orders);
        swipeRefreshOrders = view.findViewById(R.id.swipe_refresh_orders);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerOrders.setAdapter(new OrderAdapter());

        sharedPreferences = requireActivity().getSharedPreferences("FlowerShopPrefs", MODE_PRIVATE);

        progressBarOrders.setVisibility(View.VISIBLE);
        loadAssignedOrders();

        swipeRefreshOrders.setOnRefreshListener(() -> {
            Log.d(TAG, "Refreshing order list...");
            loadAssignedOrders();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void loadAssignedOrders() {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Log.e(TAG, "Token is empty");
            Toast.makeText(getContext(), "Không tìm thấy token xác thực", Toast.LENGTH_SHORT).show();
            progressBarOrders.setVisibility(View.GONE);
            return;
        }

        ShipperService shipperService = RetrofitClient.getClient().create(ShipperService.class);
        Call<List<Order>> call = shipperService.getMyOrders("Bearer " + token);
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBarOrders.setVisibility(View.GONE);
                swipeRefreshOrders.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    tvOrderCount.setText(orders.size() + " đơn hàng");
                    if (!orders.isEmpty()) {
                        ((OrderAdapter) recyclerOrders.getAdapter()).updateData(orders);
                        tvEmptyMessageOrders.setVisibility(View.GONE);
                        recyclerOrders.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyMessageOrders.setVisibility(View.VISIBLE);
                        recyclerOrders.setVisibility(View.GONE);
                    }
                } else {
                    tvEmptyMessageOrders.setText("Lỗi: " + response.code());
                    tvEmptyMessageOrders.setVisibility(View.VISIBLE);
                    recyclerOrders.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBarOrders.setVisibility(View.GONE);
                swipeRefreshOrders.setRefreshing(false);
                tvEmptyMessageOrders.setText("Lỗi: " + t.getMessage());
                tvEmptyMessageOrders.setVisibility(View.VISIBLE);
                recyclerOrders.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data != null ? data.getExtras() : null;
            if (extras != null) {
                capturedBitmap = (Bitmap) extras.get("data");
                showConfirmationDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "Quyền camera bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận giao hàng")
                .setMessage("Bạn đã giao hàng thành công?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    if (capturedBitmap != null) {
                        uploadDeliveryProof(capturedBitmap);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void uploadDeliveryProof(Bitmap bitmap) {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy token xác thực", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
        MultipartBody.Part proofImage = MultipartBody.Part.createFormData("proofImage", "delivery.jpg", requestFile);

        ShipperService shipperService = RetrofitClient.getClient().create(ShipperService.class);
        Call<Void> call = shipperService.completeDelivery("Bearer " + token, currentOrderId, proofImage);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Hoàn thành đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    loadAssignedOrders();
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show();
        }
    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order> orderList = new ArrayList<>();
        private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public void updateData(List<Order> newData) {
            orderList.clear();
            orderList.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shipper_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            holder.tvOrderId.setText("Mã đơn: " + order.getId());
            holder.tvCustomerName.setText("Khách hàng: " + order.getAccountId().getFullName());
            holder.tvTotalAmount.setText("Tổng tiền: " + order.getTotalAmount() + " VND");
            holder.tvAddress.setText("Địa chỉ: " + order.getAddressShip());

            try {
                Date orderAt = isoFormat.parse(order.getOrderAt());
                holder.tvOrderAt.setText("Đặt lúc: " + displayFormat.format(orderAt));
            } catch (ParseException e) {
                holder.tvOrderAt.setText("Đặt lúc: Không hợp lệ");
            }

            holder.tvStatus.setText("Trạng thái: " + order.getStatus());

            if (order.getStatus().equalsIgnoreCase("delivered")) {
                // Đơn đã giao: nút map đổi màu, nút hoàn tất đổi text và disable
                holder.btnViewMap.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
                holder.btnViewMap.setEnabled(false);

                holder.btnCompleteDelivery.setText("Đã hoàn tất");
                holder.btnCompleteDelivery.setEnabled(false);
                holder.btnCompleteDelivery.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
            } else {
                // Đơn chưa giao: nút map và hoàn tất bình thường
                holder.btnViewMap.setEnabled(true);
                holder.btnViewMap.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)); // "#64B5F6"

                holder.btnCompleteDelivery.setEnabled(true);
                holder.btnCompleteDelivery.setText("Hoàn tất");
                holder.btnCompleteDelivery.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bottom_nav_color)); // "#388E3C"

                holder.btnViewMap.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), MapActivity.class);
                    intent.putExtra("destinationAddress", order.getAddressShip());
                    startActivity(intent);
                });

                holder.btnCompleteDelivery.setOnClickListener(v -> {
                    currentOrderId = order.getId();
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                    } else {
                        dispatchTakePictureIntent();
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return orderList.size();
        }

        public class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvCustomerName, tvTotalAmount, tvAddress, tvOrderAt, tvStatus;
            Button btnViewMap, btnCompleteDelivery;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tv_order_id);
                tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
                tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
                tvAddress = itemView.findViewById(R.id.tv_address);
                tvOrderAt = itemView.findViewById(R.id.tv_order_at);
                tvStatus = itemView.findViewById(R.id.tv_status);
                btnViewMap = itemView.findViewById(R.id.btn_view_map);
                btnCompleteDelivery = itemView.findViewById(R.id.btn_complete_delivery);
            }
        }
    }
}
