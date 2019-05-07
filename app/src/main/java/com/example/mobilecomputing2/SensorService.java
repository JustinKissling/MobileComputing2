package com.example.mobilecomputing2;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

public class SensorService extends Service implements SensorEventListener {

    private LocationListener locationListener;
    private LocationManager locationManager;
    private SensorManager sensorManager;

    private Sensor accelSensor;
    private Sensor gyroSensor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        // Init managers
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Init sensors
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Register listeners for sensors
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("current_coordinates");
                i.putExtra("coordinates", "Lng: " + location.getLongitude() + "; Lat: " + location.getLatitude());
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // If accelerometer values changed
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Intent i = new Intent("current_accelerometer");
            i.putExtra("accelerometer", "X: " + String.valueOf(event.values[0]) + "; Y: " + String.valueOf(event.values[1]) + "; Z: " + String.valueOf(event.values[2]));
            sendBroadcast(i);
        }

        // If gyroscope values changed
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Intent i = new Intent("current_gyroscope");
            i.putExtra("gyroscope", "X: " + String.valueOf(event.values[0]) + "; Y: " + String.valueOf(event.values[1]) + "; Z: " + String.valueOf(event.values[2]));
            sendBroadcast(i);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
