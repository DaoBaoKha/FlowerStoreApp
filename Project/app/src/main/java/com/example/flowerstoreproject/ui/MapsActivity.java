package com.example.flowerstoreproject.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.flowerstoreproject.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private String destinationAddress; // Địa chỉ đích từ đơn hàng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        destinationAddress = getIntent().getStringExtra("destination_address");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && destinationAddress != null) {
                showRoute(location, destinationAddress);
            } else {
                Toast.makeText(this, "Unable to get current location or destination", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRoute(Location currentLocation, String destinationAddress) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(destinationAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address destination = addresses.get(0);
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                LatLng destinationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());

                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Shipper's location"));
                mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Delivery address"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));

                mMap.addPolyline(new PolylineOptions().add(currentLatLng, destinationLatLng).width(8).color(0xFF2196F3));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to find address", Toast.LENGTH_SHORT).show();
        }
    }
}
