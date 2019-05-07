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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textAccel;
    private TextView textGPS;
    private TextView textGyro;

    private String currentGPS = "";
    private String currentAccel = "";
    private String currentGyro = "";

    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final int PERIOD = 2000;

    private Handler mHandlerThread;

    private BroadcastReceiver broadcastReceiver;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            textGPS.setText(currentGPS);
            textAccel.setText(currentAccel);
            textGyro.setText(currentGyro);

            timerHandler.postDelayed(this, PERIOD);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (!checkPermission())
            {
                requestPermission();
            } else {
                Intent i = new Intent(this, SensorService.class);
                startService(i);
            }
        }

        timerHandler.postDelayed(timerRunnable, 0);

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
                        currentGPS = "GPS coordinates: " + intent.getExtras().get("coordinates");
                    }
                    // If accelerometer values received
                    if (intent.getExtras().get("accelerometer") != null) {
                        currentAccel = "Acceleration data: " + intent.getExtras().get("accelerometer");
                    }
                    // If gyroscope values received
                    if (intent.getExtras().get("gyroscope") != null) {
                        currentGyro = "Gyroscope data: " + intent.getExtras().get("gyroscope");
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
        timerHandler.removeCallbacks(timerRunnable);
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


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            Toast.makeText(this, "This app requires permission to access your GPS data.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted");
                    Intent i = new Intent(this, SensorService.class);
                    startService(i);
                } else {
                    Log.e("value", "Permission Denied");
                }
                break;
        }
    }
}
