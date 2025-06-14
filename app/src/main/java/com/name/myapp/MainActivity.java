package com.name.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long UPDATE_INTERVAL = 3000; // 3 seconds
    private static final long FASTEST_INTERVAL = 2000; // 2 seconds

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    // GPS Components
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // UI Components
    private TextView tvLatitude, tvLongitude, tvSpeed, tvAccuracy, tvAltitude, tvGpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupNavigationDrawer();
        setupLocationServices();
        
        // Request location permission and start GPS updates
        if (checkLocationPermission()) {
            startLocationUpdates();
        } else {
            requestLocationPermission();
        }
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
        
        drawerToggle = new ActionBarDrawerToggle(
            this, drawerLayout, R.string.menu_open, R.string.menu_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                
                for (Location location : locationResult.getLocations()) {
                    updateLocationUI(location);
                }
            }
        };
    }

    private void updateLocationUI(Location location) {
        if (location != null) {
            tvLatitude.setText(String.format(Locale.getDefault(), "%.6f°", location.getLatitude()));
            tvLongitude.setText(String.format(Locale.getDefault(), "%.6f°", location.getLongitude()));
            
            if (location.hasSpeed()) {
                float speedKmh = location.getSpeed() * 3.6f; // Convert m/s to km/h
                tvSpeed.setText(String.format(Locale.getDefault(), "%.1f km/h", speedKmh));
            } else {
                tvSpeed.setText("--");
            }
            
            if (location.hasAccuracy()) {
                tvAccuracy.setText(String.format(Locale.getDefault(), "%.1f m", location.getAccuracy()));
            } else {
                tvAccuracy.setText("--");
            }
            
            if (location.hasAltitude()) {
                tvAltitude.setText(String.format(Locale.getDefault(), "%.1f m", location.getAltitude()));
            } else {
                tvAltitude.setText("--");
            }
            
            tvGpsStatus.setText("GPS Active - Updated every 3s");
            tvGpsStatus.setTextColor(getColor(R.color.colorGPSActive));
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            tvGpsStatus.setText("GPS Active - Updated every 3s");
            tvGpsStatus.setTextColor(getColor(R.color.colorGPSActive));
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        tvGpsStatus.setText("GPS Inactive");
        tvGpsStatus.setTextColor(getColor(R.color.colorGPSInactive));
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
            LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
                tvGpsStatus.setText("GPS Permission Denied");
                tvGpsStatus.setTextColor(getColor(R.color.colorGPSInactive));
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_tracking) {
            if (checkLocationPermission()) {
                startLocationUpdates();
                Toast.makeText(this, "GPS tracking started", Toast.LENGTH_SHORT).show();
            } else {
                requestLocationPermission();
            }
        } else if (id == R.id.nav_history) {
            Toast.makeText(this, "History option selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_export) {
            Toast.makeText(this, "Export option selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_clear) {
            stopLocationUpdates();
            clearLocationData();
            Toast.makeText(this, "GPS tracking stopped", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings option selected", Toast.LENGTH_SHORT).show();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearLocationData() {
        tvLatitude.setText("--");
        tvLongitude.setText("--");
        tvSpeed.setText("--");
        tvAccuracy.setText("--");
        tvAltitude.setText("--");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
