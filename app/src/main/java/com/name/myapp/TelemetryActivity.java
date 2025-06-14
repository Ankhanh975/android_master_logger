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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class TelemetryActivity extends AppCompatActivity implements SensorEventListener {
    
    private static final String TAG = "TelemetryActivity";
    private static final int MAX_DATA_POINTS = 100;
    private static final int UPDATE_INTERVAL_MS = 100; // 10 FPS
    
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor lightSensor;
    private Sensor proximitySensor;
    private Sensor pressureSensor;
    private Sensor temperatureSensor;
    private Sensor humiditySensor;
    
    // Charts
    private LineChart accelerationChart;
    private LineChart gyroscopeChart;
    private LineChart magnetometerChart;
    
    // Data arrays
    private List<Entry> accelerationXEntries = new ArrayList<>();
    private List<Entry> accelerationYEntries = new ArrayList<>();
    private List<Entry> accelerationZEntries = new ArrayList<>();
    private List<Entry> gyroscopeXEntries = new ArrayList<>();
    private List<Entry> gyroscopeYEntries = new ArrayList<>();
    private List<Entry> gyroscopeZEntries = new ArrayList<>();
    private List<Entry> magnetometerXEntries = new ArrayList<>();
    private List<Entry> magnetometerYEntries = new ArrayList<>();
    private List<Entry> magnetometerZEntries = new ArrayList<>();
    
    // Text views for real-time values
    private TextView tvAcceleration;
    private TextView tvGyroscope;
    private TextView tvMagnetometer;
    private TextView tvLight;
    private TextView tvProximity;
    private TextView tvPressure;
    private TextView tvTemperature;
    private TextView tvHumidity;
    
    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private int dataPointIndex = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telemetry);
        
        setupToolbar();
        initializeSensors();
        initializeViews();
        setupCharts();
        startDataCollection();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Telemetry Data");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }
    
    private void initializeSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        }
    }
    
    private void initializeViews() {
        accelerationChart = findViewById(R.id.accelerationChart);
        gyroscopeChart = findViewById(R.id.gyroscopeChart);
        magnetometerChart = findViewById(R.id.magnetometerChart);
        
        tvAcceleration = findViewById(R.id.tvAcceleration);
        tvGyroscope = findViewById(R.id.tvGyroscope);
        tvMagnetometer = findViewById(R.id.tvMagnetometer);
        tvLight = findViewById(R.id.tvLight);
        tvProximity = findViewById(R.id.tvProximity);
        tvPressure = findViewById(R.id.tvPressure);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
    }
    
    private void setupCharts() {
        setupChart(accelerationChart, "Acceleration (m/s²)");
        setupChart(gyroscopeChart, "Gyroscope (rad/s)");
        setupChart(magnetometerChart, "Magnetometer (μT)");
    }
    
    private void setupChart(LineChart chart, String label) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setBackgroundColor(getResources().getColor(android.R.color.white));
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fs", value / 10.0f);
            }
        });
        
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
    }
    
    private void startDataCollection() {
        // Register sensor listeners
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (humiditySensor != null) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                updateAccelerationData(event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:
                updateGyroscopeData(event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                updateMagnetometerData(event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_LIGHT:
                updateLightData(event.values[0]);
                break;
            case Sensor.TYPE_PROXIMITY:
                updateProximityData(event.values[0]);
                break;
            case Sensor.TYPE_PRESSURE:
                updatePressureData(event.values[0]);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                updateTemperatureData(event.values[0]);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                updateHumidityData(event.values[0]);
                break;
        }
    }
    
    private void updateAccelerationData(float x, float y, float z) {
        runOnUiThread(() -> {
            tvAcceleration.setText(String.format("X: %.2f, Y: %.2f, Z: %.2f m/s²", x, y, z));
            
            // Add data points to charts
            accelerationXEntries.add(new Entry(dataPointIndex, x));
            accelerationYEntries.add(new Entry(dataPointIndex, y));
            accelerationZEntries.add(new Entry(dataPointIndex, z));
            
            // Limit data points
            if (accelerationXEntries.size() > MAX_DATA_POINTS) {
                accelerationXEntries.remove(0);
                accelerationYEntries.remove(0);
                accelerationZEntries.remove(0);
            }
            
            updateAccelerationChart();
        });
    }
    
    private void updateGyroscopeData(float x, float y, float z) {
        runOnUiThread(() -> {
            tvGyroscope.setText(String.format("X: %.3f, Y: %.3f, Z: %.3f rad/s", x, y, z));
            
            gyroscopeXEntries.add(new Entry(dataPointIndex, x));
            gyroscopeYEntries.add(new Entry(dataPointIndex, y));
            gyroscopeZEntries.add(new Entry(dataPointIndex, z));
            
            if (gyroscopeXEntries.size() > MAX_DATA_POINTS) {
                gyroscopeXEntries.remove(0);
                gyroscopeYEntries.remove(0);
                gyroscopeZEntries.remove(0);
            }
            
            updateGyroscopeChart();
        });
    }
    
    private void updateMagnetometerData(float x, float y, float z) {
        runOnUiThread(() -> {
            tvMagnetometer.setText(String.format("X: %.1f, Y: %.1f, Z: %.1f μT", x, y, z));
            
            magnetometerXEntries.add(new Entry(dataPointIndex, x));
            magnetometerYEntries.add(new Entry(dataPointIndex, y));
            magnetometerZEntries.add(new Entry(dataPointIndex, z));
            
            if (magnetometerXEntries.size() > MAX_DATA_POINTS) {
                magnetometerXEntries.remove(0);
                magnetometerYEntries.remove(0);
                magnetometerZEntries.remove(0);
            }
            
            updateMagnetometerChart();
        });
    }
    
    private void updateLightData(float value) {
        runOnUiThread(() -> {
            tvLight.setText(String.format("Light: %.1f lux", value));
        });
    }
    
    private void updateProximityData(float value) {
        runOnUiThread(() -> {
            tvProximity.setText(String.format("Proximity: %.1f cm", value));
        });
    }
    
    private void updatePressureData(float value) {
        runOnUiThread(() -> {
            tvPressure.setText(String.format("Pressure: %.1f hPa", value));
        });
    }
    
    private void updateTemperatureData(float value) {
        runOnUiThread(() -> {
            tvTemperature.setText(String.format("Temperature: %.1f°C", value));
        });
    }
    
    private void updateHumidityData(float value) {
        runOnUiThread(() -> {
            tvHumidity.setText(String.format("Humidity: %.1f%%", value));
        });
    }
    
    private void updateAccelerationChart() {
        LineDataSet dataSetX = new LineDataSet(accelerationXEntries, "X-Axis");
        dataSetX.setColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSetX.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSetX.setDrawValues(false);
        
        LineDataSet dataSetY = new LineDataSet(accelerationYEntries, "Y-Axis");
        dataSetY.setColor(getResources().getColor(android.R.color.holo_green_dark));
        dataSetY.setCircleColor(getResources().getColor(android.R.color.holo_green_dark));
        dataSetY.setDrawValues(false);
        
        LineDataSet dataSetZ = new LineDataSet(accelerationZEntries, "Z-Axis");
        dataSetZ.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSetZ.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSetZ.setDrawValues(false);
        
        LineData lineData = new LineData(dataSetX, dataSetY, dataSetZ);
        accelerationChart.setData(lineData);
        accelerationChart.invalidate();
    }
    
    private void updateGyroscopeChart() {
        LineDataSet dataSetX = new LineDataSet(gyroscopeXEntries, "X-Axis");
        dataSetX.setColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSetX.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSetX.setDrawValues(false);
        
        LineDataSet dataSetY = new LineDataSet(gyroscopeYEntries, "Y-Axis");
        dataSetY.setColor(getResources().getColor(android.R.color.holo_green_dark));
        dataSetY.setCircleColor(getResources().getColor(android.R.color.holo_green_dark));
        dataSetY.setDrawValues(false);
        
        LineDataSet dataSetZ = new LineDataSet(gyroscopeZEntries, "Z-Axis");
        dataSetZ.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSetZ.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSetZ.setDrawValues(false);
        
        LineData lineData = new LineData(dataSetX, dataSetY, dataSetZ);
        gyroscopeChart.setData(lineData);
        gyroscopeChart.invalidate();
    }
    
    private void updateMagnetometerChart() {
        LineDataSet dataSetX = new LineDataSet(magnetometerXEntries, "X-Axis");
        dataSetX.setColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSetX.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSetX.setDrawValues(false);
        
        LineDataSet dataSetY = new LineDataSet(magnetometerYEntries, "Y-Axis");
        dataSetY.setColor(getResources().getColor(android.R.color.holo_green_dark));
        dataSetY.setCircleColor(getResources().getColor(android.R.color.holo_green_dark));
        dataSetY.setDrawValues(false);
        
        LineDataSet dataSetZ = new LineDataSet(magnetometerZEntries, "Z-Axis");
        dataSetZ.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSetZ.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSetZ.setDrawValues(false);
        
        LineData lineData = new LineData(dataSetX, dataSetY, dataSetZ);
        magnetometerChart.setData(lineData);
        magnetometerChart.invalidate();
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
} 