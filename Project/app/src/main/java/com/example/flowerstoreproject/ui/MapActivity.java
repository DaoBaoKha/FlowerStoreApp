package com.example.flowerstoreproject.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.DirectionsService;
import com.example.flowerstoreproject.api.services.GeocodingService;
import com.example.flowerstoreproject.model.DirectionsResponse;
import com.example.flowerstoreproject.model.GeocodingResponse;
import com.example.flowerstoreproject.model.Result;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private final String shipperAddress = "FPT University, Ho Chi Minh City";
    private String destinationAddress;
    private Button btnBack;
    private LatLng shipperLatLng;
    private LatLng destinationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        destinationAddress = getIntent().getStringExtra("destinationAddress");

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnBack = findViewById(R.id.btn_back_to_orders);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        fetchCoordinatesAndDrawMarker(shipperAddress, true);
        fetchCoordinatesAndDrawMarker(destinationAddress, false);
    }

    private void fetchCoordinatesAndDrawMarker(String address, boolean isShipper) {
        if (address == null || address.isEmpty()) {
            Log.e("Geocoding", "Địa chỉ null hoặc rỗng: " + (isShipper ? "Shipper" : "Giao hàng"));
            return;
        }

        GeocodingService service = RetrofitClient.getGoogleApiClient().create(GeocodingService.class);
        Call<GeocodingResponse> call = service.getLocation(address, RetrofitClient.GOOGLE_API_KEY);

        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().getResults().isEmpty()) {
                        Result result = response.body().getResults().get(0);
                        double lat = result.getGeometry().getLocation().getLat();
                        double lng = result.getGeometry().getLocation().getLng();
                        LatLng location = new LatLng(lat, lng);

                        googleMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(isShipper ? "Shipper Location" : "Delivery Address"));

                        if (isShipper) {
                            shipperLatLng = location;
                        } else {
                            destinationLatLng = location;
                        }

                        // Nếu có cả hai tọa độ, điều chỉnh camera và vẽ đường đi
                        if (shipperLatLng != null && destinationLatLng != null) {
                            LatLngBounds bounds = new LatLngBounds.Builder()
                                    .include(shipperLatLng)
                                    .include(destinationLatLng)
                                    .build();
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                            // Gọi Directions API để vẽ đường đi
                            fetchAndDrawDirections();
                        }
                    } else {
                        Log.e("Geocoding", "Không tìm thấy kết quả cho địa chỉ: " + address);
                    }
                } else {
                    Log.e("Geocoding", "API trả về lỗi cho địa chỉ: " + address + ", mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e("Geocoding", "Lỗi khi gọi API cho địa chỉ: " + address, t);
            }
        });
    }

    private void fetchAndDrawDirections() {
        DirectionsService service = RetrofitClient.getGoogleApiClient().create(DirectionsService.class);
        String origin = shipperLatLng.latitude + "," + shipperLatLng.longitude;
        String destination = destinationLatLng.latitude + "," + destinationLatLng.longitude;

        Call<DirectionsResponse> call = service.getDirections(origin, destination, RetrofitClient.GOOGLE_API_KEY);
        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DirectionsResponse.Route> routes = response.body().getRoutes();
                    if (!routes.isEmpty()) {
                        String polylinePoints = routes.get(0).getOverviewPolyline().getPoints();
                        List<LatLng> decodedPath = decodePolyline(polylinePoints);

                        // Vẽ đường đi
                        googleMap.addPolyline(new PolylineOptions()
                                .addAll(decodedPath)
                                .color(0xFF0000FF) // Màu xanh
                                .width(10));
                    } else {
                        Log.e("Directions", "Không tìm thấy lộ trình");
                    }
                } else {
                    Log.e("Directions", "API Directions trả về lỗi, mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e("Directions", "Lỗi khi gọi API Directions", t);
            }
        });
    }

    // Hàm giải mã polyline từ chuỗi encoded
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }
        return poly;
    }
}