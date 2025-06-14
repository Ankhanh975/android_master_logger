package com.name.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Locale;

public class GpsActivity extends AppCompatActivity {

    private static final String TAG = "GpsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long UPDATE_INTERVAL = 3000; // 3 seconds
    private static final long FASTEST_INTERVAL = 2000; // 2 seconds

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
        
        try {
            Log.d(TAG, "Setting content view");
            setContentView(R.layout.activity_gps);
            Log.d(TAG, "Content view set successfully");

            // Setup toolbar
            setupToolbar();
            
            // Initialize views
            setupViews();
            
            // Initialize GPS services
            setupLocationServices();
            setupGnssStatusCallback();
            
            // Request location permission and start GPS updates
            if (checkLocationPermission()) {
                startLocationUpdates();
                startGnssStatusUpdates();
            } else {
                requestLocationPermission();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to load GPS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                Log.d(TAG, "Toolbar found, setting up");
                setSupportActionBar(toolbar);
                
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("GPS Data");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                    Log.d(TAG, "Action bar configured");
                } else {
                    Log.w(TAG, "getSupportActionBar() returned null");
                }
            } else {
                Log.w(TAG, "Toolbar not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage(), e);
        }
    }

    private void setupViews() {
        try {
            Log.d(TAG, "Finding GPS views");
            
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
            
            Log.d(TAG, "All GPS views found successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupLocationServices() {
        try {
            Log.d(TAG, "Setting up location services");
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
            
            Log.d(TAG, "Location services setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up location services: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up location services: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupGnssStatusCallback() {
        try {
            Log.d(TAG, "Setting up GNSS status callback");
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
            Log.d(TAG, "GNSS status callback setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up GNSS status callback: " + e.getMessage(), e);
        }
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            startLocationUpdates();
            startGnssStatusUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (locationManager != null && gnssStatusCallback != null) {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
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