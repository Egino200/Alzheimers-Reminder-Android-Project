package com.example.yearproject12;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeofenceHelp extends ContextWrapper {

    private static final String TAG = "GeofenceHelp";
    PendingIntent pendingIntent;

    public GeofenceHelp(Context base) {
        super(base);
    }


    public GeofencingRequest getGeofencingRequest(Geofence geofence) {
        //create a geofencing request, its initial trigger is set to dwell
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .build();
    }

    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitionTypes) {
        //create a geofence object with the given parameters
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public PendingIntent getPendingIntent() {
        if (pendingIntent != null) {
            return pendingIntent;
        }

        Intent intent = new Intent(this, GeofenceBroadcastReciever.class);
        pendingIntent = PendingIntent.getBroadcast(this, 2607, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        return pendingIntent;
    }

    public String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            return GeofenceStatusCodes.getStatusCodeString(apiException.getStatusCode());
        }
        return e.getLocalizedMessage();
    }
}
