package com.example.flowerstoreproject.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.FlowerService;
import com.example.flowerstoreproject.model.ApiResponse;
import com.example.flowerstoreproject.model.Flower;
import com.example.flowerstoreproject.model.FlowerRequest;
import com.example.flowerstoreproject.model.FlowerUpdateRequest;
import com.example.flowerstoreproject.ui.admin.adapters.FlowerAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFlowersFragment extends Fragment implements FlowerAdapter.OnFlowerClickListener {
    private static final String TAG = "AdminFlowersFragment";

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddFlower;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private EditText etSearch;

    private FlowerAdapter flowerAdapter;
    private List<Flower> flowerList;
    private List<Flower> filteredFlowerList;
    private FlowerService flowerService;
    private SharedPreferences sharedPreferences;
    private String authToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_flowers, container, false);

        initViews(view);
        setupAuth();
        setupRecyclerView();
        setupFAB();
        setupSwipeRefresh();
        setupSearch();

        loadFlowers();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_flowers);
        fabAddFlower = view.findViewById(R.id.fab_add_flower);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        etSearch = view.findViewById(R.id.et_search_flowers);
    }

    private void setupAuth() {
        sharedPreferences = getActivity().getSharedPreferences("FlowerShopPrefs", getActivity().MODE_PRIVATE);
        authToken = "Bearer " + sharedPreferences.getString("token", "");
        flowerService = RetrofitClient.getClient().create(FlowerService.class);
    }

    private void setupRecyclerView() {
        flowerList = new ArrayList<>();
        filteredFlowerList = new ArrayList<>();
        flowerAdapter = new FlowerAdapter(getContext(), filteredFlowerList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(flowerAdapter);
    }

    private void setupFAB() {
        fabAddFlower.setOnClickListener(v -> {
            showAddEditFlowerDialog(null);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFlowers();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFlowers(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterFlowers(String query) {
        filteredFlowerList.clear();
        if (query.isEmpty()) {
            filteredFlowerList.addAll(flowerList);
        } else {
            for (Flower flower : flowerList) {
                if (flower.getName().toLowerCase().contains(query.toLowerCase()) ||
                        flower.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                        (flower.getCategory() != null && flower.getCategory().getName().toLowerCase().contains(query.toLowerCase()))) {
                    filteredFlowerList.add(flower);
                }
            }
        }
        flowerAdapter.notifyDataSetChanged();
    }

    private void loadFlowers() {
        Log.d(TAG, "loadFlowers: Loading flowers from API");
        progressBar.setVisibility(View.VISIBLE);

        Call<List<Flower>> call = flowerService.getAllFlowers();
        call.enqueue(new Callback<List<Flower>>() {
            @Override
            public void onResponse(Call<List<Flower>> call, Response<List<Flower>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadFlowers: Successfully loaded " + response.body().size() + " flowers");
                    flowerList.clear();
                    flowerList.addAll(response.body());
                    filteredFlowerList.clear();
                    filteredFlowerList.addAll(flowerList);
                    flowerAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "loadFlowers: Failed to load flowers, response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to load flowers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Flower>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "loadFlowers: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditFlowerDialog(Flower flower) {
        AddEditFlowerDialog dialog = new AddEditFlowerDialog(getContext(), flower, new AddEditFlowerDialog.OnFlowerSaveListener() {
            @Override
            public void onFlowerSaved(FlowerRequest flowerRequest) {
                if (flower == null) {
                    createFlower(flowerRequest);
                } else {
                    updateFlower(flower.getId(), new FlowerUpdateRequest(
                            flowerRequest.getName(),
                            flowerRequest.getPrice(),
                            flowerRequest.getDescription(),
                            flowerRequest.getImage(),
                            flowerRequest.getStock()
                    ));
                }
            }
        });
        dialog.show();
    }

    private void createFlower(FlowerRequest flowerRequest) {
        Log.d(TAG, "createFlower: Creating new flower");
        progressBar.setVisibility(View.VISIBLE);

        Call<ApiResponse<Flower>> call = flowerService.createFlower(authToken, flowerRequest);
        call.enqueue(new Callback<ApiResponse<Flower>>() {
            @Override
            public void onResponse(Call<ApiResponse<Flower>> call, Response<ApiResponse<Flower>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "createFlower: Successfully created flower");
                    Toast.makeText(getContext(), "Flower created successfully", Toast.LENGTH_SHORT).show();
                    loadFlowers(); // Reload to get updated data
                } else {
                    Log.e(TAG, "createFlower: Failed to create flower, response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to create flower", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Flower>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "createFlower: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFlower(String flowerId, FlowerUpdateRequest flowerUpdateRequest) {
        Log.d(TAG, "updateFlower: Updating flower with ID: " + flowerId);
        progressBar.setVisibility(View.VISIBLE);

        Call<ApiResponse<Flower>> call = flowerService.updateFlower(authToken, String.valueOf(flowerId), flowerUpdateRequest);
        call.enqueue(new Callback<ApiResponse<Flower>>() {
            @Override
            public void onResponse(Call<ApiResponse<Flower>> call, Response<ApiResponse<Flower>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "updateFlower: Successfully updated flower");
                    Toast.makeText(getContext(), "Flower updated successfully", Toast.LENGTH_SHORT).show();
                    loadFlowers(); // Reload to get updated data
                } else {
                    Log.e(TAG, "updateFlower: Failed to update flower, response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to update flower", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Flower>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "updateFlower: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFlower(String flowerId) {
        Log.d(TAG, "deleteFlower: Deleting flower with ID: " + flowerId);
        progressBar.setVisibility(View.VISIBLE);

        Call<ApiResponse<Void>> call = flowerService.deleteFlower(authToken, flowerId+"");
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Log.d(TAG, "deleteFlower: Successfully deleted flower");
                    Toast.makeText(getContext(), "Flower deleted successfully", Toast.LENGTH_SHORT).show();
                    loadFlowers(); // Reload to get updated data
                } else {
                    Log.e(TAG, "deleteFlower: Failed to delete flower, response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to delete flower", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "deleteFlower: Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Flower flower) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Flower")
                .setMessage("Are you sure you want to delete \"" + flower.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteFlower(flower.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // FlowerAdapter.OnFlowerClickListener implementation
    @Override
    public void onFlowerClick(Flower flower) {
        // Handle flower item click - could show details or edit
        showAddEditFlowerDialog(flower);
    }

    @Override
    public void onEditClick(Flower flower) {
        showAddEditFlowerDialog(flower);
    }

    @Override
    public void onDeleteClick(Flower flower) {
        showDeleteConfirmationDialog(flower);
    }
}