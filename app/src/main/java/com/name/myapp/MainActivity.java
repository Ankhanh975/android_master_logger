package com.name.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long UPDATE_INTERVAL = 1000; // 1 second
    private static final long FASTEST_INTERVAL = 500; // 500ms

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    // GPS Components
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // UI Components
    private TextView tvGpsStatus, tvLatitude, tvLongitude, tvSpeed, tvAccuracy, tvAltitude;
    private TextView tvSessionDuration, tvPointsRecorded;
    private MaterialButton btnStartStop, btnViewHistory;

    // Tracking State
    private boolean isTracking = false;
    private long sessionStartTime = 0;
    private int pointsRecorded = 0;
    private List<Location> locationHistory = new ArrayList<>();
    private Handler sessionHandler = new Handler(Looper.getMainLooper());
    private Runnable sessionRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupNavigationDrawer();
        setupLocationServices();
        setupClickListeners();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvSessionDuration = findViewById(R.id.tvSessionDuration);
        tvPointsRecorded = findViewById(R.id.tvPointsRecorded);
        
        btnStartStop = findViewById(R.id.btnStartStop);
        btnViewHistory = findViewById(R.id.btnViewHistory);
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
                    if (isTracking) {
                        locationHistory.add(location);
                        pointsRecorded++;
                        updateSessionInfo();
                    }
                }
            }
        };
    }

    private void setupClickListeners() {
        btnStartStop.setOnClickListener(v -> toggleTracking());
        btnViewHistory.setOnClickListener(v -> showHistory());
        
        sessionRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    updateSessionDuration();
                    sessionHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void toggleTracking() {
        if (!isTracking) {
            startTracking();
        } else {
            stopTracking();
        }
    }

    private void startTracking() {
        if (checkLocationPermission()) {
            isTracking = true;
            sessionStartTime = System.currentTimeMillis();
            pointsRecorded = 0;
            locationHistory.clear();
            
            btnStartStop.setText(R.string.stop_tracking);
            btnStartStop.setBackgroundTintList(getColorStateList(R.color.colorError));
            tvGpsStatus.setText(R.string.tracking_active);
            tvGpsStatus.setTextColor(getColor(R.color.colorGPSActive));
            
            startLocationUpdates();
            sessionHandler.post(sessionRunnable);
            
            Toast.makeText(this, R.string.tracking_started, Toast.LENGTH_SHORT).show();
        } else {
            requestLocationPermission();
        }
    }

    private void stopTracking() {
        isTracking = false;
        
        btnStartStop.setText(R.string.start_tracking);
        btnStartStop.setBackgroundTintList(getColorStateList(R.color.colorSuccess));
        tvGpsStatus.setText(R.string.tracking_inactive);
        tvGpsStatus.setTextColor(getColor(R.color.colorGPSInactive));
        
        stopLocationUpdates();
        sessionHandler.removeCallbacks(sessionRunnable);
        
        Toast.makeText(this, R.string.tracking_stopped, Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
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
        }
    }

    private void updateSessionDuration() {
        if (sessionStartTime > 0) {
            long elapsed = System.currentTimeMillis() - sessionStartTime;
            long hours = elapsed / (1000 * 60 * 60);
            long minutes = (elapsed % (1000 * 60 * 60)) / (1000 * 60);
            long seconds = (elapsed % (1000 * 60)) / 1000;
            
            String duration = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            tvSessionDuration.setText(duration);
        }
    }

    private void updateSessionInfo() {
        tvPointsRecorded.setText(String.valueOf(pointsRecorded));
    }

    private void showHistory() {
        if (locationHistory.isEmpty()) {
            Toast.makeText(this, R.string.no_history, Toast.LENGTH_SHORT).show();
        } else {
            String message = String.format("Recorded %d GPS points", locationHistory.size());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
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
                startTracking();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_tracking) {
            toggleTracking();
        } else if (id == R.id.nav_history) {
            showHistory();
        } else if (id == R.id.nav_export) {
            exportData();
        } else if (id == R.id.nav_clear) {
            clearHistory();
        } else if (id == R.id.nav_settings) {
            showSettings();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void exportData() {
        if (locationHistory.isEmpty()) {
            Toast.makeText(this, R.string.no_history, Toast.LENGTH_SHORT).show();
        } else {
            // Simple export - just show a toast for now
            Toast.makeText(this, R.string.data_exported, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearHistory() {
        locationHistory.clear();
        pointsRecorded = 0;
        updateSessionInfo();
        Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
    }

    private void showSettings() {
        Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
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
        if (sessionHandler != null) {
            sessionHandler.removeCallbacks(sessionRunnable);
        }
    }
}
