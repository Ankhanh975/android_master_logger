package com.name.myapp;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class GpsGuideActivity extends AppCompatActivity {

    private static final String TAG = "GpsGuideActivity";
    private TextView tvGpsGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "Setting content view");
            setContentView(R.layout.activity_gps_guide);
            Log.d(TAG, "Content view set successfully");

            // Setup toolbar with error handling
            setupToolbar();
            
            // Initialize views with error handling
            setupViews();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to load GPS guide: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    getSupportActionBar().setTitle("GPS Accuracy Guide");
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
            Log.d(TAG, "Finding GPS guide text view");
            tvGpsGuide = findViewById(R.id.tvGpsGuide);
            
            if (tvGpsGuide != null) {
                Log.d(TAG, "GPS guide text view found, loading content");
                
                // Load GPS guide content
                String guideText = GpsGuideData.getGpsGuideText();
                if (guideText != null && !guideText.isEmpty()) {
                    tvGpsGuide.setText(guideText);
                    Log.d(TAG, "GPS guide text loaded successfully");
                } else {
                    Log.w(TAG, "GPS guide text is null or empty");
                    tvGpsGuide.setText("GPS guide content not available");
                }
                
                // Enable scrolling
                tvGpsGuide.setMovementMethod(new ScrollingMovementMethod());
                Log.d(TAG, "Scrolling movement method set");
                
            } else {
                Log.e(TAG, "GPS guide text view not found");
                Toast.makeText(this, "Error: Could not find GPS guide text view", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navigate up pressed");
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destroyed");
    }
} 