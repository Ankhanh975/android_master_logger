package com.name.myapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TelemetryActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "TelemetryActivity";
    private static final int UPDATE_INTERVAL = 3000; // 3 seconds

    // UI Components
    private TextView tvSensorStatus;
    private TextView tvAccelX, tvAccelY, tvAccelZ, tvAccelMagnitude;
    private TextView tvRotationAzimuth, tvRotationPitch, tvRotationRoll;
    private TextView tvUpdateRate, tvSensorAccuracy, tvSensorInfo;

    // Sensor Management
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor rotationVector;
    private Handler updateHandler;
    private Runnable updateRunnable;

    // Data Storage
    private float[] accelerationValues = new float[3];
    private float[] rotationValues = new float[3];
    private boolean sensorsActive = false;
    private long lastUpdateTime = 0;
    private int updateCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "Setting content view");
            setContentView(R.layout.activity_telemetry);
            Log.d(TAG, "Content view set successfully");

            // Setup toolbar
            setupToolbar();
            
            // Initialize views
            setupViews();
            
            // Initialize sensors
            setupSensors();
            
            // Setup periodic updates
            setupPeriodicUpdates();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to load telemetry: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    getSupportActionBar().setTitle("Device Telemetry");
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
            Log.d(TAG, "Finding telemetry views");
            
            // Status views
            tvSensorStatus = findViewById(R.id.tvSensorStatus);
            
            // Acceleration views
            tvAccelX = findViewById(R.id.tvAccelX);
            tvAccelY = findViewById(R.id.tvAccelY);
            tvAccelZ = findViewById(R.id.tvAccelZ);
            tvAccelMagnitude = findViewById(R.id.tvAccelMagnitude);
            
            // Rotation views
            tvRotationAzimuth = findViewById(R.id.tvRotationAzimuth);
            tvRotationPitch = findViewById(R.id.tvRotationPitch);
            tvRotationRoll = findViewById(R.id.tvRotationRoll);
            
            // Sensor info views
            tvUpdateRate = findViewById(R.id.tvUpdateRate);
            tvSensorAccuracy = findViewById(R.id.tvSensorAccuracy);
            tvSensorInfo = findViewById(R.id.tvSensorInfo);
            
            Log.d(TAG, "All telemetry views found successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupSensors() {
        try {
            Log.d(TAG, "Setting up sensors");
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            
            if (sensorManager != null) {
                // Get accelerometer sensor
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (accelerometer != null) {
                    Log.d(TAG, "Accelerometer sensor found");
                } else {
                    Log.w(TAG, "Accelerometer sensor not available");
                }
                
                // Get rotation vector sensor
                rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                if (rotationVector != null) {
                    Log.d(TAG, "Rotation vector sensor found");
                } else {
                    Log.w(TAG, "Rotation vector sensor not available");
                }
                
                updateSensorInfo();
            } else {
                Log.e(TAG, "Sensor manager is null");
                Toast.makeText(this, "Sensor manager not available", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up sensors: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up sensors: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupPeriodicUpdates() {
        try {
            Log.d(TAG, "Setting up periodic updates");
            updateHandler = new Handler(Looper.getMainLooper());
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    updateDisplay();
                    updateHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            };
            
            // Start periodic updates
            updateHandler.post(updateRunnable);
            Log.d(TAG, "Periodic updates started");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up periodic updates: " + e.getMessage(), e);
        }
    }

    private void updateSensorInfo() {
        try {
            StringBuilder info = new StringBuilder();
            
            if (accelerometer != null) {
                info.append("Accelerometer:\n");
                info.append("• Name: ").append(accelerometer.getName()).append("\n");
                info.append("• Vendor: ").append(accelerometer.getVendor()).append("\n");
                info.append("• Version: ").append(accelerometer.getVersion()).append("\n");
                info.append("• Max Range: ").append(accelerometer.getMaximumRange()).append(" m/s²\n");
                info.append("• Resolution: ").append(accelerometer.getResolution()).append(" m/s²\n");
                info.append("• Power: ").append(accelerometer.getPower()).append(" mA\n\n");
            }
            
            if (rotationVector != null) {
                info.append("Rotation Vector:\n");
                info.append("• Name: ").append(rotationVector.getName()).append("\n");
                info.append("• Vendor: ").append(rotationVector.getVendor()).append("\n");
                info.append("• Version: ").append(rotationVector.getVersion()).append("\n");
                info.append("• Max Range: ").append(rotationVector.getMaximumRange()).append(" rad\n");
                info.append("• Resolution: ").append(rotationVector.getResolution()).append(" rad\n");
                info.append("• Power: ").append(rotationVector.getPower()).append(" mA\n");
            }
            
            if (info.length() == 0) {
                info.append("No sensors available");
            }
            
            if (tvSensorInfo != null) {
                tvSensorInfo.setText(info.toString());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating sensor info: " + e.getMessage(), e);
        }
    }

    private void updateDisplay() {
        try {
            DecimalFormat df = new DecimalFormat("#.##");
            
            // Update acceleration values
            if (tvAccelX != null) {
                tvAccelX.setText(df.format(accelerationValues[0]) + " m/s²");
            }
            if (tvAccelY != null) {
                tvAccelY.setText(df.format(accelerationValues[1]) + " m/s²");
            }
            if (tvAccelZ != null) {
                tvAccelZ.setText(df.format(accelerationValues[2]) + " m/s²");
            }
            if (tvAccelMagnitude != null) {
                float magnitude = (float) Math.sqrt(
                    accelerationValues[0] * accelerationValues[0] +
                    accelerationValues[1] * accelerationValues[1] +
                    accelerationValues[2] * accelerationValues[2]
                );
                tvAccelMagnitude.setText(df.format(magnitude) + " m/s²");
            }
            
            // Update rotation values (convert to degrees)
            if (tvRotationAzimuth != null) {
                float azimuth = (float) Math.toDegrees(rotationValues[0]);
                tvRotationAzimuth.setText(df.format(azimuth) + "°");
            }
            if (tvRotationPitch != null) {
                float pitch = (float) Math.toDegrees(rotationValues[1]);
                tvRotationPitch.setText(df.format(pitch) + "°");
            }
            if (tvRotationRoll != null) {
                float roll = (float) Math.toDegrees(rotationValues[2]);
                tvRotationRoll.setText(df.format(roll) + "°");
            }
            
            // Update status and metrics
            updateStatus();
            updateMetrics();
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating display: " + e.getMessage(), e);
        }
    }

    private void updateStatus() {
        try {
            if (tvSensorStatus != null) {
                if (sensorsActive) {
                    tvSensorStatus.setText("Sensors Active");
                } else {
                    tvSensorStatus.setText("Sensors Inactive");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating status: " + e.getMessage(), e);
        }
    }

    private void updateMetrics() {
        try {
            long currentTime = System.currentTimeMillis();
            updateCount++;
            
            if (lastUpdateTime > 0) {
                long timeDiff = currentTime - lastUpdateTime;
                float updateRate = 1000.0f / timeDiff; // updates per second
                
                if (tvUpdateRate != null) {
                    DecimalFormat df = new DecimalFormat("#.#");
                    tvUpdateRate.setText(df.format(updateRate) + " Hz");
                }
            }
            
            lastUpdateTime = currentTime;
            
            // Update accuracy based on sensor availability
            if (tvSensorAccuracy != null) {
                if (accelerometer != null && rotationVector != null) {
                    tvSensorAccuracy.setText("High");
                } else if (accelerometer != null || rotationVector != null) {
                    tvSensorAccuracy.setText("Medium");
                } else {
                    tvSensorAccuracy.setText("Low");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating metrics: " + e.getMessage(), e);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            sensorsActive = true;
            
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values, 0, accelerationValues, 0, 3);
                    break;
                    
                case Sensor.TYPE_ROTATION_VECTOR:
                    // Convert rotation vector to Euler angles
                    float[] rotationMatrix = new float[9];
                    float[] orientationAngles = new float[3];
                    
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                    SensorManager.getOrientation(rotationMatrix, orientationAngles);
                    
                    System.arraycopy(orientationAngles, 0, rotationValues, 0, 3);
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in sensor changed: " + e.getMessage(), e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
        Log.d(TAG, "Sensor accuracy changed: " + sensor.getName() + " = " + accuracy);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed");
        
        try {
            // Register sensor listeners
            if (sensorManager != null) {
                if (accelerometer != null) {
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    Log.d(TAG, "Accelerometer listener registered");
                }
                
                if (rotationVector != null) {
                    sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
                    Log.d(TAG, "Rotation vector listener registered");
                }
            }
            
            // Restart periodic updates
            if (updateHandler != null && updateRunnable != null) {
                updateHandler.post(updateRunnable);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity paused");
        
        try {
            // Unregister sensor listeners
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
                Log.d(TAG, "Sensor listeners unregistered");
            }
            
            // Stop periodic updates
            if (updateHandler != null && updateRunnable != null) {
                updateHandler.removeCallbacks(updateRunnable);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onPause: " + e.getMessage(), e);
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
        
        try {
            // Clean up
            if (updateHandler != null && updateRunnable != null) {
                updateHandler.removeCallbacks(updateRunnable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }
} 