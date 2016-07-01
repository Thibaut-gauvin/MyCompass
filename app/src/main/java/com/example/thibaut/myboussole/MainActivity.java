package com.example.thibaut.myboussole;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public ImageView iv;

    public SensorManager mSensorManager;
    public Sensor mOrientation;
    public Sensor mMagnetic;

    public float[] magneticSensorValues = new float[3];
    public float[] accelerometerSensorValues = new float[3];

    public LocationManager locationManager;
    public LocationListener locationListener;

    public TextView latitudeText;
    public TextView longitudeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        latitudeText = (TextView) findViewById(R.id.latitudeValue);
        longitudeText = (TextView) findViewById(R.id.longitudeValue);

        latitudeText.setText("calculating...");
        longitudeText.setText("calculating...");

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);

        iv = (ImageView) findViewById(R.id.image_boussoule);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            /**
             * This event is called every time location change
             *
             * @param location The current Location coordinate
             */
            @Override
            public void onLocationChanged(Location location) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();

                latitudeText.setText(String.format("%.4g%n", currentLatitude));
                longitudeText.setText(String.format("%.4g%n", currentLongitude));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}

        };

        // Check for users permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
            requestPermissions(permissions, 0);
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /**
     * Called when the accuracy of a sensor has changed.
     * Listen Magnetic & Orientation sensor
     *
     * @param sensorEvent The current sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if (sensorEvent.sensor == mMagnetic) {
            magneticSensorValues[0] = sensorEvent.values[0];
            magneticSensorValues[1] = sensorEvent.values[1];
            magneticSensorValues[2] = sensorEvent.values[2];
        } else if (sensorEvent.sensor == mOrientation) {
            accelerometerSensorValues[0] = sensorEvent.values[0];
            accelerometerSensorValues[1] = sensorEvent.values[1];
            accelerometerSensorValues[2] = sensorEvent.values[2];
        }

        if (accelerometerSensorValues != null && magneticSensorValues != null) {
            float matriceCartesienVersPolaire[] = new float[9];
            float matricePolaireVersCartesien[] = new float[9];

            if (mSensorManager.getRotationMatrix(matriceCartesienVersPolaire, matricePolaireVersCartesien, accelerometerSensorValues, magneticSensorValues)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(matriceCartesienVersPolaire, orientation);

                float azimut = orientation[0];
                float rotation = -azimut * 360 / (2 * 3.14159f);

                // System.out.println("ROTATION: " + rotation);
                changeAngle(rotation, iv);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    /**
     * Change angle rotation of compass
     *
     * @param angle The current angle
     * @param iv The compass image view
     */
    public void changeAngle(float angle, ImageView iv)
    {
        float currentAngle = iv.getRotation();

        iv.setPivotX(iv.getWidth() / 2);
        iv.setPivotY(iv.getHeight() / 2);

        if(Math.abs(currentAngle - angle) > 10) {
            iv.setRotation(angle);
        }
    }
}
