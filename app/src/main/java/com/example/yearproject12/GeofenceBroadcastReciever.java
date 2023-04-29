package com.example.yearproject12;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.Locale;

public class GeofenceBroadcastReciever extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onReceive(Context context, Intent intent) {


        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        GeofencingEvent  geofencingEvent=  GeofencingEvent.fromIntent(intent);
        NotificationHelper notificationHelper = new NotificationHelper(context);
        List<Geofence> getgeofenceList = geofencingEvent.getTriggeringGeofences();
//        for(Geofence geofence: getgeofenceList){
//            Log.d("GEO", "onReceive: "+ geofence.getRequestId());
//        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        //a switch case which checks the transition type and sends a notification if entering or exiting a geofence
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d("GEO", "onReceive: " + "enter");
                notificationHelper.sendNotification("Geofence", "Welcome back", MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d("GEO", "onReceive: " + "exit");
                notificationHelper.sendNotification("Geofence", "You have left the geofence", MapsActivity.class);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("6505551212", null, "I have left my geofence. Please check up on me ASAP", null, null);
                break;
           case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d("GEO", "onReceive: " + "dwell");
                break;
        }
    }
}