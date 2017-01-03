package com.example.locationapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.location.Location;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.locationapp.R.id.map;

import android.widget.ToggleButton;

import static java.lang.Math.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener, ConnectionCallbacks, OnConnectionFailedListener, SensorEventListener {

    private GoogleMap mMap;

    public final static String EXTRA_MESSAGE = "com.example.firstapp.MESSAGE";
    private ToggleButton flashlightSwitch;
    private TextView locationTextView;
    private TextView gyroscopeTextView;
    private TextView lightTextView;
    private TextView compassTextView;
    public static GoogleApiClient mGoogleApiClient;
    private SensorManager mSensorManager;
    private Sensor gyroscopeSensor;
    private Sensor lightSensor;
    private final static double EPSILON = 0.00001;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private boolean hasFlash;
    private Camera camera;
    private Camera.Parameters parameters;
    private String mLatitudeText;
    private String mLongitudeText;
    private LocationRequest mLocationRequest;
    private LatLng yourLocalization;
    private int currentZoom;

    private Location mCurrentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        locationTextView = (TextView) findViewById(R.id.locationTextView);
        gyroscopeTextView = (TextView) findViewById(R.id.gyroscopeTextView);
        lightTextView = (TextView) findViewById(R.id.lightTextView);
        flashlightSwitch = (ToggleButton) findViewById(R.id.flashlightSwitch);
        compassTextView = (TextView) findViewById(R.id.compassTextView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);

        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (hasFlash) {

            camera = Camera.open();
            parameters = camera.getParameters();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //new LocationUpdate(this).execute();
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient.reconnect();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        flashOff();

    }
    protected void onDestroy()
    {
        super.onDestroy();
        camera.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        flashOff();


    }

    public void light(View view) {
        if (hasFlash == true) {
            if (!view.isSelected()) {
                flashOn();
                view.setSelected(true);
            } else if (view.isSelected()) {
                flashOff();
                view.setSelected(false);
            }
        }
    }

    public void flashOn() {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        camera.startPreview();
    }

    public void flashOff() {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
        camera.stopPreview();
    }


    @Override
    public void onConnected(Bundle connectionHint) {


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        boolean mRequestingLocationUpdates=true;
        updateLocationUI(mLastLocation);
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void updateLocationUI(Location location) {
        StringBuffer latitudeSymbol = new StringBuffer();
        StringBuffer longitudeSymbol = new StringBuffer();
        if (location != null) {

            if (location.getLatitude() <= 90) {
                latitudeSymbol.replace(0, 1, "N");
            } else {
                latitudeSymbol.replace(0, 1, "S");
            }
            if (location.getLongitude() >= 0 && location.getLongitude() <= 180) {
                longitudeSymbol.replace(0, 1, "E");
            } else {
                longitudeSymbol.replace(0, 1, "W");
            }
            mLatitudeText = String.valueOf(location.getLatitude());
            mLongitudeText = String.valueOf(location.getLongitude());

            locationTextView.setText("Your location : \n" + mLatitudeText + " " + latitudeSymbol + " " + mLongitudeText + " " + longitudeSymbol);
        } else {
            locationTextView.setText("Location failed");
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLatitudeText = String.valueOf(mCurrentLocation.getLatitude());
        mLongitudeText = String.valueOf(mCurrentLocation.getLongitude());

        updateLocationUI(mCurrentLocation);

        refreshMaps(mMap);

        }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    public void refresh(View view)
    {

        //locationReconnect();
    }

    public void locationReconnect()
    {
        mGoogleApiClient.reconnect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        System.out.println("connection failed");
    }
    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection Suspended");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if( event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                gyroscopeTextView.setText("Gyroscope values: \n" + axisX + "\n" + axisY + "\n" + axisZ);

                // Calculate the angular speed of the sample
                float omegaMagnitude = (float) sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) sin(thetaOverTwo);
                float cosThetaOverTwo = (float) cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
            }
            timestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

        }
        if( event.sensor.getType() == Sensor.TYPE_LIGHT)
        {
            lightTextView.setText("Light value: \n" + event.values[0] + " lux" );

        }
        if( event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            compassTextView.setText("Magnetic field value: \n" + event.values[0] + " "+event.values[1] + " "+event.values[2] + " μT" );
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



    @Override
    public void onMapReady(GoogleMap map) {


        currentZoom=12;
        mMap = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        refreshMaps(map);


    }
    public void refreshMaps(GoogleMap map)
    {
        try {
            yourLocalization = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            map.clear();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocalization, map.getCameraPosition().zoom));
            map.addMarker(new MarkerOptions().position(yourLocalization).title("Marker in your localization"));
        }
        catch(java.lang.NullPointerException e){

        }
    }

    public void showMaps(View view)
    {
        Intent intent= new Intent(this, MapsActivity.class);
        String message=mLatitudeText+" "+mLongitudeText;

        intent.putExtra(EXTRA_MESSAGE,message);
        startActivity(intent);
    }
}








