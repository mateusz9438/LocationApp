package com.example.locationapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    public static GoogleApiClient mGoogleApiClient;
    Double latitude;
    Double longtitude;
    private LocationRequest mLocationRequest;
    private LatLng yourLocalization;
    private int currentZoom;
    private Location mCurrentLocation;
    private List<LatLng>locationHistory;
    Polyline line;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        locationHistory = new ArrayList<LatLng>();
        System.out.println(" on create maps");
    }


    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();
    }

    protected void onStart() {

        super.onStart();


    }

    protected void onDestroy()
    {
        super.onDestroy();
        //mGoogleApiClient.disconnect();
        locationHistory.clear();

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        locationHistory.add(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
        refreshMaps(mMap);

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
            LatLng lastLoc=new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            map.addMarker(new MarkerOptions().position(lastLoc).title("Marker in your localization"));
            map.addMarker(new MarkerOptions().position(yourLocalization).title("Marker in your localization"));
        }
        catch(java.lang.NullPointerException e){

        }
        System.out.println(locationHistory.size());
        line=mMap.addPolyline(new PolylineOptions()
                .addAll(locationHistory)
                .width(5)
                .color(Color.RED));


    }




     protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    Location mLastLocation;
    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        boolean mRequestingLocationUpdates=true;

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        try{
            locationHistory.add(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
        }
        catch (NullPointerException e)
        {

        }


    }

    @Override
    public void onConnectionSuspended(int i) {

       System.out.println("Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        System.out.println("connection failed");
    }

    public void back(View view)
    {
        finish();
    }

}


