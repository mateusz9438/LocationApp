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

import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

import com.google.android.gms.location.LocationServices;

import android.widget.ToggleButton;

import static java.lang.Math.*;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,OnConnectionFailedListener,SensorEventListener {


    private ToggleButton flashlightSwitch;
    private TextView locationTextView;
    private TextView gyroscopeTextView;
    private TextView lightTextView;
    private TextView compassTextView;
    private GoogleApiClient mGoogleApiClient;
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
    public final static String EXTRA_MESSAGE = "com.example.firstapp.MESSAGE";
    private String mLatitudeText;
    private String mLongitudeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        locationTextView=(TextView) findViewById(R.id.locationTextView);
        gyroscopeTextView=(TextView) findViewById(R.id.gyroscopeTextView) ;
        lightTextView=(TextView) findViewById(R.id.lightTextView);
        flashlightSwitch=(ToggleButton) findViewById(R.id.flashlightSwitch);
        compassTextView=(TextView) findViewById(R.id.compassTextView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        lightSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL );

        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if(hasFlash) {

            camera = Camera.open();
            parameters = camera.getParameters();
        }

    }

    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient.connect();

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

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        flashOff();
    }

    public void light(View view){
        if(hasFlash==true)
        {
            if(!view.isSelected()) {
                flashOn();
                view.setSelected(true);
            }else if(view.isSelected()){
                flashOff();
                view.setSelected(false);
            }
        }
    }


    public void flashOn()
    {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        camera.startPreview();
    }
    public void flashOff()
    {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
        camera.stopPreview();
    }

    public void showMaps(View view)
    {

        if(mLatitudeText!=null &&  mLongitudeText!=null)
        {

            Intent intent = new Intent(this, MapsActivity.class);
            double latitude=Double.parseDouble(mLatitudeText);
            double longtitude=Double.parseDouble(mLongitudeText);

            double[] values= new double[2];
            values[0]=latitude;
            values[1]=longtitude;
            intent.putExtra(EXTRA_MESSAGE, values);
            startActivity(intent);
        }




    }
    @Override
    public void onConnected(Bundle connectionHint) {


        StringBuffer latitudeSymbol=new StringBuffer();
        StringBuffer longitudeSymbol=new StringBuffer();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            if(mLastLocation.getLatitude()<=90)
            {
                latitudeSymbol.replace(0,1,"N");
            }
            else
            {
                latitudeSymbol.replace(0,1,"S");
            }
            if(mLastLocation.getLongitude()>=0 && mLastLocation.getLongitude()<=180 )
            {
               longitudeSymbol.replace(0,1,"E");
            }
            else
            {
                longitudeSymbol.replace(0,1,"W");
            }
            mLatitudeText=String.valueOf(mLastLocation.getLatitude());
            mLongitudeText=String.valueOf(mLastLocation.getLongitude());

            locationTextView.setText("Your location : \n"+mLatitudeText+" "+latitudeSymbol+" "+mLongitudeText+" " + longitudeSymbol);
        }
        else
        {
            locationTextView.setText("Location failed");
        }

    }

    public void refresh(View view)
    {
        StringBuffer latitudeSymbol=new StringBuffer();
        StringBuffer longitudeSymbol=new StringBuffer();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            if(mLastLocation.getLatitude()<=90)
            {
                latitudeSymbol.replace(0,1,"N");
            }
            else
            {
                latitudeSymbol.replace(0,1,"S");
            }
            if(mLastLocation.getLongitude()>=0 && mLastLocation.getLongitude()<=180 )
            {
                longitudeSymbol.replace(0,1,"E");
            }
            else
            {
                longitudeSymbol.replace(0,1,"W");
            }
            mLatitudeText=String.valueOf(mLastLocation.getLatitude());
            mLongitudeText=String.valueOf(mLastLocation.getLongitude());

            locationTextView.setText("Your location : \n"+mLatitudeText+" "+latitudeSymbol+" "+mLongitudeText+" " + longitudeSymbol);
        }
        else
        {
            locationTextView.setText("Location failed");
        }

    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }
    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection Suspended");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // This time step's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.


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
}







