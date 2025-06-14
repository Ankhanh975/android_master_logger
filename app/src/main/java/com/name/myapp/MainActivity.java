package com.name.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
    private LocationManager locationManager;
    private GnssStatus.Callback gnssStatusCallback;

    // UI Components
    private TextView tvLatitude, tvLongitude, tvSpeed, tvAccuracy, tvAltitude, tvGpsStatus;
    private TextView tvSatelliteCount, tvSatelliteInfo, tvSignalStrength, tvProviderInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupNavigationDrawer();
        setupLocationServices();
        setupGnssStatusCallback();
        
        // Request location permission and start GPS updates
        if (checkLocationPermission()) {
            startLocationUpdates();
            startGnssStatusUpdates();
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
        tvSatelliteCount = findViewById(R.id.tvSatelliteCount);
        tvSatelliteInfo = findViewById(R.id.tvSatelliteInfo);
        tvSignalStrength = findViewById(R.id.tvSignalStrength);
        tvProviderInfo = findViewById(R.id.tvProviderInfo);
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
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
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

    private void setupGnssStatusCallback() {
        gnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                updateProviderInfo("GNSS Started");
            }

            @Override
            public void onStopped() {
                updateProviderInfo("GNSS Stopped");
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                updateProviderInfo("First Fix: " + ttffMillis + "ms");
            }

            @Override
            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                updateSatelliteInfo(status);
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

    private void updateSatelliteInfo(GnssStatus status) {
        int satelliteCount = status.getSatelliteCount();
        tvSatelliteCount.setText(String.valueOf(satelliteCount));
        
        StringBuilder satelliteInfo = new StringBuilder();
        StringBuilder signalInfo = new StringBuilder();
        
        int usedSatellites = 0;
        int strongSignals = 0;
        
        for (int i = 0; i < satelliteCount; i++) {
            int constellationType = status.getConstellationType(i);
            float cn0DbHz = status.getCn0DbHz(i);
            boolean usedInFix = status.usedInFix(i);
            
            if (usedInFix) {
                usedSatellites++;
            }
            
            if (cn0DbHz > 30) {
                strongSignals++;
            }
            
            String constellation = getConstellationName(constellationType);
            satelliteInfo.append(String.format("%s: %.1fdB ", constellation, cn0DbHz));
            
            if (usedInFix) {
                satelliteInfo.append("✓ ");
            }
            satelliteInfo.append("\n");
        }
        
        tvSatelliteInfo.setText(satelliteInfo.toString());
        tvSignalStrength.setText(String.format("Used: %d, Strong: %d", usedSatellites, strongSignals));
    }

    private String getConstellationName(int constellationType) {
        switch (constellationType) {
            case GnssStatus.CONSTELLATION_GPS:
                return "GPS";
            case GnssStatus.CONSTELLATION_GLONASS:
                return "GLONASS";
            case GnssStatus.CONSTELLATION_BEIDOU:
                return "BeiDou";
            case GnssStatus.CONSTELLATION_GALILEO:
                return "Galileo";
            case GnssStatus.CONSTELLATION_QZSS:
                return "QZSS";
            case GnssStatus.CONSTELLATION_SBAS:
                return "SBAS";
            case GnssStatus.CONSTELLATION_IRNSS:
                return "IRNSS";
            default:
                return "Unknown";
        }
    }

    private void updateProviderInfo(String info) {
        tvProviderInfo.setText(info);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            tvGpsStatus.setText("GPS Active - Updated every 3s");
            tvGpsStatus.setTextColor(getColor(R.color.colorGPSActive));
        }
    }

    private void startGnssStatusUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                locationManager.registerGnssStatusCallback(gnssStatusCallback);
            }
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
                startLocationUpdates();
                startGnssStatusUpdates();
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
        
        if (id == R.id.nav_gps) {
            Toast.makeText(this, "GPS Data", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_gps_guide) {
            showGpsGuide();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showGpsGuide() {
        try {
            Toast.makeText(this, "Opening GPS Guide...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, GpsGuideActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening GPS Guide: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (locationManager != null && gnssStatusCallback != null) {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
        }
    }
}
