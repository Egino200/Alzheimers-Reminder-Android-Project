package com.example.yearproject12;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.yearproject12.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private GeofenceHelp geofenceHelp;
    private GoogleMap mMap;
    double lat;
    double longe;
    private GeofencingClient geofencingClient;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference geo_location = database.getReference().child("Geofence");
    private ActivityMapsBinding binding;
    Button mapbutton;
    private String GEOFENCE_ID = "GEOFENCE_ID";
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelp = new GeofenceHelp(this);
        mapbutton = findViewById(R.id.mapbutton);

        //Button to go back to main activity
        mapbutton.setOnClickListener(view -> {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            Log.d("SMS", "onCreate: Permission Granted");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 10003);
        }


    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng ireland = new LatLng(53.3498, -6.2603);
        enableUserLocation();

        //add value listener to get the lat and long from the database
        geo_location.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    //set the geofence to the previously stored geofence location
                    lat = snapshot.child("Lat").getValue(Double.class);
                    longe = snapshot.child("Long").getValue(Double.class);
                    LatLng latLng = new LatLng(lat, longe);

                    addMarker(latLng);
                    addCircle(latLng, 200);
                    addGeofence(latLng, 200);


                } catch (Exception e) {
                    Log.d("GEO", "onDataChange: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //focus the map on ireland initially
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ireland));
        mMap.setOnMapLongClickListener(this);
    }

    //request permission to use the users location
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            //if permission is granted then enable the users location
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                }

            }
        }
        //if permission is granted then enable the users location
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("GEO", "onRequestPermissionsResult: " + "Permission granted");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMapLongClick(LatLng latLng) {
        //when the map is clicked and held create a new geofence
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //clears previous geofence and adds a new one
            mMap.clear();
            addMarker(latLng);
            addCircle(latLng, 200);
            geo_location.child("Lat").setValue(latLng.latitude);
            geo_location.child("Long").setValue(latLng.longitude);
            addGeofence(latLng, 200);
        } else {
            //if permission is not granted then request it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            }

        }
        //if permission is not granted then request it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 10002);
        } else {

        }

        //if permission is not granted then request it (Many permissions are required)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        } else {

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addGeofence(LatLng latLng, float radius) {
        //add the geofence to the map and set the transition types
        Geofence geofence = geofenceHelp.getGeofence(GEOFENCE_ID, latLng, 200, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelp.getGeofencingRequest(geofence);
        //create a pending intent to be used when the geofence is triggered
        PendingIntent pendingIntent = geofenceHelp.getPendingIntent();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);

        }
        //add the geofence to the geofencing client
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("GEO", "onSuccess: " + "yea yea");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("GEO", "onFailure: " + "yea naw" + e.getMessage());
                    }
                });


//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            // Permission is not granted, request it
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
//        }else{
//            Geofence geofence = geofenceHelp.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
//          GeofencingRequest geofenceRequest = geofenceHelp.getGeofencingRequest(geofence);
//
//
//
//          PendingIntent pendingIntent = geofenceHelp.getPendingIntent();
//
//
//          geofencingClient.addGeofences(geofenceRequest, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
//              @Override
//              public void onSuccess(Void unused) {
//                    Log.d("GEO", "onSuccess: Geofence Added...");
//              }
//          }).addOnFailureListener(new OnFailureListener() {
//              @Override
//              public void onFailure(@NonNull Exception e) {
//                  String errorMessage = geofenceHelp.getErrorString(e);
//                  Log.d("GEO", "onFailure: " + errorMessage);
//              }
//          });
//        }






    }
    private void addMarker(LatLng latLng){
        //add a marker to the map
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        //adds a red circle to the map activity
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(60, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
}
