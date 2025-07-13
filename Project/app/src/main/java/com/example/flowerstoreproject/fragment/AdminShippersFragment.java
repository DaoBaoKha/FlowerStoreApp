package com.example.flowerstoreproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.ShipperService;
import com.example.flowerstoreproject.model.Shipper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminShippersFragment extends Fragment {

    private RecyclerView recyclerShippers;
    private TextView tvShipperCount, tvEmptyMessageShippers;
    private ProgressBar progressBarShippers;
    private SwipeRefreshLayout swipeRefreshShippers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_shippers, container, false);

        recyclerShippers = view.findViewById(R.id.recycler_shippers);
        tvShipperCount = view.findViewById(R.id.tv_shipper_count);
        tvEmptyMessageShippers = view.findViewById(R.id.tv_empty_message_shippers);
        progressBarShippers = view.findViewById(R.id.progress_bar_shippers);
        swipeRefreshShippers = view.findViewById(R.id.swipe_refresh_shippers);

        recyclerShippers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerShippers.setAdapter(new ShipperAdapter());

        // Hiển thị ProgressBar khi tải dữ liệu
        progressBarShippers.setVisibility(View.VISIBLE);

        // Gọi API lấy danh sách shipper
        ShipperService shipperService = RetrofitClient.getClient().create(ShipperService.class);
        Call<List<Shipper>> call = shipperService.getAllShippers();
        call.enqueue(new Callback<List<Shipper>>() {
            @Override
            public void onResponse(Call<List<Shipper>> call, Response<List<Shipper>> response) {
                progressBarShippers.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Shipper> shippers = response.body();
                    tvShipperCount.setText(shippers.size() + " shippers");
                    if (!shippers.isEmpty()) {
                        ((ShipperAdapter) recyclerShippers.getAdapter()).updateData(shippers);
                        tvEmptyMessageShippers.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptyMessageShippers.setText("Failed to load shippers. Error: " + response.code());
                    tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Shipper>> call, Throwable t) {
                progressBarShippers.setVisibility(View.GONE);
                tvEmptyMessageShippers.setText("Error: " + t.getMessage());
                tvEmptyMessageShippers.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý làm mới
        swipeRefreshShippers.setOnRefreshListener(() -> {
            call.clone().enqueue(new Callback<List<Shipper>>() {
                @Override
                public void onResponse(Call<List<Shipper>> call, Response<List<Shipper>> response) {
                    swipeRefreshShippers.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Shipper> shippers = response.body();
                        tvShipperCount.setText(shippers.size() + " shippers");
                        if (!shippers.isEmpty()) {
                            ((ShipperAdapter) recyclerShippers.getAdapter()).updateData(shippers);
                            tvEmptyMessageShippers.setVisibility(View.GONE);
                        } else {
                            tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Shipper>> call, Throwable t) {
                    swipeRefreshShippers.setRefreshing(false);
                    tvEmptyMessageShippers.setText("Error: " + t.getMessage());
                    tvEmptyMessageShippers.setVisibility(View.VISIBLE);
                }
            });
        });

        return view;
    }

    private class ShipperAdapter extends RecyclerView.Adapter<ShipperAdapter.ShipperViewHolder> {
        private List<Shipper> shipperList = new ArrayList<>();

        public void updateData(List<Shipper> newData) {
            shipperList.clear();
            shipperList.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_shippers, parent, false);
            return new ShipperViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ShipperViewHolder holder, int position) {
            Shipper shipper = shipperList.get(position);
            holder.tvShipperId.setText(shipper.getId());
            holder.tvShipperName.setText(shipper.getFullName());
            holder.tvShipperEmail.setText(shipper.getEmail());
            holder.tvShipperPhone.setText(shipper.getPhone());
        }

        @Override
        public int getItemCount() {
            return shipperList.size();
        }

        public class ShipperViewHolder extends RecyclerView.ViewHolder {
            TextView tvShipperId, tvShipperName, tvShipperEmail, tvShipperPhone;

            public ShipperViewHolder(@NonNull View itemView) {
                super(itemView);
                tvShipperId = itemView.findViewById(R.id.tvShipperId);
                tvShipperName = itemView.findViewById(R.id.tvShipperName);
                tvShipperEmail = itemView.findViewById(R.id.tvShipperEmail);
                tvShipperPhone = itemView.findViewById(R.id.tvShipperPhone);
            }
        }
    }
}