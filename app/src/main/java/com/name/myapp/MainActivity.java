package com.name.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    // UI Components
    private TextView tvGpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupNavigationDrawer();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        
        // Set initial status
        tvGpsStatus.setText("Use navigation menu to access GPS features");
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_gps) {
            showGps();
        } else if (id == R.id.nav_telemetry) {
            showTelemetry();
        } else if (id == R.id.nav_gps_guide) {
            showGpsGuide();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showGps() {
        try {
            Toast.makeText(this, "Opening GPS...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, GpsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening GPS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showTelemetry() {
        try {
            Toast.makeText(this, "Opening Telemetry...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, TelemetryActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening Telemetry: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
}
