package com.neeri.wbis.tools;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.neeri.wbis.CreateRecord;

public class GPSLocation implements LocationListener {
    private static final String TAG = "GPSLocation";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Activity activity;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public GPSLocation(Activity activity){
        super();
        this.activity = activity;

        findLocation();
    }

    private void findLocation(){
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Check and correct device location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(locationSettingsResponse -> {
            Log.d(TAG, "findLocation: All device location settings are satisfied");
        });

        task.addOnFailureListener((exc) -> {
            Log.d(TAG, "findLocation: device location settings not are satisfied");
            if (exc instanceof ResolvableApiException) {
                Log.d(TAG, "findLocation: prompting user to change device location settings");
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) exc;
                    resolvable.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Toast.makeText(activity, "Location found: " + locationResult.getLocations().size(), Toast.LENGTH_SHORT).show();
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            CreateRecord.setLocation(location);
//                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                } else {
                    Log.e(TAG, "getLastLocation: location object is null");
                    Toast.makeText(activity, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                }
            }
        };


        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
//                    Toast.makeText(activity, "Location fetched successfully", Toast.LENGTH_SHORT).show();
                    CreateRecord.setLocation(location);
                } else {
                    Log.e(TAG, "getLastLocation: location object is null");
                    Toast.makeText(activity, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(activity, "LAT : "+location.getLatitude() + " LON : "+location.getLatitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
