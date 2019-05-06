package com.example.mobilecomputing2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textAccel;
    private TextView textGPS;
    private TextView textGyro;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        checkPermissions();
    }

    private void initUI() {
        textGPS = (TextView) findViewById(R.id.textGPS);
        textAccel = (TextView) findViewById(R.id.textAccel);
        textGyro = (TextView) findViewById(R.id.textGyro);

        textGPS.setText("GPS coordinates: unknown");
        textAccel.setText("Acceleration data: unknown");
        textGyro.setText("Gyroscope data: unknown");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // If GPS coordinates received
                    if (intent.getExtras().get("coordinates") != null) {
                        textGPS.setText("GPS coordinates: " + intent.getExtras().get("coordinates"));
                    }
                    // If accelerometer values received
                    if (intent.getExtras().get("accelerometer") != null) {
                        textAccel.setText("Acceleration data: " + intent.getExtras().get("accelerometer"));
                    }
                    // If gyroscope values received
                    if (intent.getExtras().get("gyroscope") != null) {
                        textGyro.setText("Gyroscope data: " + intent.getExtras().get("gyroscope"));
                    }
                }
            };
        }

        // Register receiver for all Intents
        registerReceiver(broadcastReceiver, new IntentFilter("current_coordinates"));
        registerReceiver(broadcastReceiver, new IntentFilter("current_accelerometer"));
        registerReceiver(broadcastReceiver, new IntentFilter("current_gyroscope"));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        Intent i = new Intent(this, SensorService.class);
        stopService(i);
    }

    /**
     * This method checks and grants all necessary permissions
     */
    private void checkPermissions() {
        // If permission not granted yet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            // If permission already granted
        } else {
            // Start service
            Intent i = new Intent(this, SensorService.class);
            startService(i);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(this, SensorService.class);
                    startService(i);
                }
                break;
            }
        }
    }
}
