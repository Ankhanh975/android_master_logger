package com.name.myapp;

import android.Manifest;
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
import androidx.appcompat.app.AlertDialog;
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
        String guideText = 
            "📍 GPS ACCURACY GUIDE\n\n" +
            "🎯 LOCATION ACCURACY\n" +
            "• Excellent: ±1-3 meters (open sky, clear view)\n" +
            "• Good: ±3-5 meters (urban areas, some obstructions)\n" +
            "• Fair: ±5-10 meters (dense urban, heavy tree cover)\n" +
            "• Poor: ±10+ meters (indoor, underground, tunnels)\n\n" +
            "📊 ALTITUDE ACCURACY\n" +
            "• GPS altitude is less accurate than horizontal position\n" +
            "• Typical accuracy: ±10-20 meters (vs ±3-5m horizontal)\n" +
            "• Barometric sensors improve altitude accuracy\n" +
            "• Elevation data may vary by ±15-30 meters\n\n" +
            "🚗 SPEED ACCURACY\n" +
            "• Excellent: ±0.5-1 km/h (constant movement)\n" +
            "• Good: ±1-2 km/h (variable speed)\n" +
            "• Poor: ±2-5 km/h (slow movement, stops/starts)\n" +
            "• Speed below 2 km/h may show as 0\n\n" +
            "📡 SATELLITE SIGNAL STRENGTH (dB-Hz)\n" +
            "• Excellent: 40+ dB-Hz (strong, clear signal)\n" +
            "• Good: 30-40 dB-Hz (reliable positioning)\n" +
            "• Fair: 20-30 dB-Hz (usable, may be less accurate)\n" +
            "• Poor: <20 dB-Hz (weak, unreliable)\n\n" +
            "🛰️ SATELLITE CONSTELLATIONS\n" +
            "• GPS (USA): 24+ satellites, global coverage\n" +
            "• GLONASS (Russia): 24 satellites, global coverage\n" +
            "• BeiDou (China): 35+ satellites, global coverage\n" +
            "• Galileo (Europe): 30 satellites, global coverage\n" +
            "• QZSS (Japan): 4 satellites, Asia-Pacific focus\n" +
            "• SBAS: Augmentation systems for improved accuracy\n\n" +
            "📈 OPTIMAL SATELLITE CONDITIONS\n" +
            "• Minimum satellites: 4 for basic positioning\n" +
            "• Good positioning: 6-8 satellites\n" +
            "• Excellent positioning: 8+ satellites\n" +
            "• Multi-constellation: Better accuracy and reliability\n\n" +
            "🌍 FACTORS AFFECTING ACCURACY\n" +
            "• Atmospheric conditions (ionosphere, troposphere)\n" +
            "• Satellite geometry (HDOP, VDOP, PDOP)\n" +
            "• Multipath interference (buildings, trees)\n" +
            "• Device hardware quality\n" +
            "• Environmental obstructions\n\n" +
            "⚡ REAL-TIME ACCURACY INDICATORS\n" +
            "• \"Used\" satellites: Actually contributing to position fix\n" +
            "• \"Strong\" signals: >30 dB-Hz, high-quality data\n" +
            "• First Fix Time: Time to acquire initial position\n" +
            "• GNSS Status: System operational state\n\n" +
            "🔧 IMPROVING ACCURACY\n" +
            "• Clear view of sky (minimize obstructions)\n" +
            "• Wait for more satellites to acquire\n" +
            "• Stay stationary for initial fix\n" +
            "• Use high-accuracy mode\n" +
            "• Enable all available constellations\n\n" +
            "📱 DEVICE-SPECIFIC CONSIDERATIONS\n" +
            "• Modern phones support multiple constellations\n" +
            "• Hardware quality varies between devices\n" +
            "• Some devices have barometric sensors\n" +
            "• Antenna quality affects signal reception\n" +
            "• Software algorithms improve accuracy\n\n" +
            "⚠️ LIMITATIONS\n" +
            "• Indoor positioning is unreliable\n" +
            "• Urban canyons reduce accuracy\n" +
            "• Weather can affect signal quality\n" +
            "• Battery optimization may reduce update frequency\n" +
            "• Some features require clear sky view";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Accuracy & Technical Guide")
               .setMessage(guideText)
               .setPositiveButton("Got it!", null)
               .setIcon(android.R.drawable.ic_menu_info_details)
               .show();
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
