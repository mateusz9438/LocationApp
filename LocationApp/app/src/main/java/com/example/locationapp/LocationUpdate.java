package com.example.locationapp;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import android.content.Context;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

/**
 * Created by Mateusz on 2016-12-03.
 */

public class LocationUpdate extends AsyncTask<Void, Void, Void> {

    Activity baseActivity;
    TextView locationTextView;


    public LocationUpdate(Activity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public LocationUpdate() {

    }

    @Override
    protected void onPreExecute() {
        locationTextView = (TextView) baseActivity.findViewById(R.id.locationTextView);

        System.out.println("Zaczynam async");
    }

    @Override
    protected Void doInBackground(Void... arg0) {


        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        //System.out.println("Skończyłem async"+licz);

        new LocationUpdate(baseActivity).execute();

    }
}
