package com.tt.t.tidytechtowns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, proximityDialog.mapDialogListener {

    private GoogleMap mMap;
    private boolean showing = false;
    private Marker bin;
    private LatLng currentLocation;
    private Button gpsbutton;
    private boolean proximity = true;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 sec */
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up function with button to display bins on map
        Button binShow = (Button) findViewById(R.id.binBtn);
        binShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change set visibility in stead to avoid global variables
                if (!showing) {
                    LatLng bin1 = new LatLng(53, -6);
                    bin = mMap.addMarker(new MarkerOptions().position(bin1).title("This is a BIN!").draggable(true));
                    Button button = (Button) findViewById(R.id.binBtn);
                    button.setText("Hide bins");
                    showing = true;
                } else {
                    bin.remove();
                    Button button = (Button) findViewById(R.id.binBtn);
                    button.setText("Show bins");
                    showing = false;
                }
            }
        });


        startLocationUpdates();

        // Function to add marker to map
        gpsbutton = (Button) findViewById(R.id.GPS);
        gpsbutton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 //final boolean[] proximity = {true};
                 if (currentLocation==null){
                     Toast.makeText(getApplicationContext(), "Acquiring locaiton ... Please try again", Toast.LENGTH_SHORT).show();
                    return;
                 }
                 else {
                     final LatLng you = new LatLng(currentLocation.latitude, currentLocation.longitude);
                     //markerDialogFragment box = new markerDialogFragment();
                     // Check if location is by existing marker
                     for (Marker marker : mMarkerArray) {

                         Location loc1 = new Location("");
                         loc1.setLatitude(marker.getPosition().latitude);
                         loc1.setLongitude(marker.getPosition().longitude);

                         Location loc2 = new Location("");
                         loc2.setLatitude(you.latitude);
                         loc2.setLongitude(you.longitude);

                         if (loc1.distanceTo(loc2) < 20) {

                             // Dialog to check if user wants to proceed
                            openDialog();

                            // for some reason does not proceed past here until function is called again...

                            // If close to one that is enough to break
                            break;
                         }
                     }

                     Toast.makeText(getApplicationContext(), "Addingmarker"+proximity, Toast.LENGTH_SHORT).show();
                     if (proximity) {
                         addMarker(you);
                     }
                 }

             }
         });
    }

    /**
     * Add marker to map at location latlon
     * @param latlon
     */
    public void addMarker(LatLng latlon){
        Marker marker = mMap.addMarker(new MarkerOptions().position(latlon).title("Yep, you!").icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMarkerArray.add(marker);
    }


    // opens dialog box if location is near an existing marker
    public void openDialog(){
        proximityDialog box = new proximityDialog();
        box.show(getSupportFragmentManager(), "Proximity check");
    }

    // uses interface to dialog box positive button
    @Override
    public void proximityPositiveClick(proximityDialog dialog) {
        proximity = true;
        Toast.makeText(getApplicationContext(), "Pressed OK"+proximity, Toast.LENGTH_SHORT).show();
    }

    // uses interface to dialog box positive button
    @Override
    public void proximityNegativeClick(proximityDialog dialog) {
        proximity = false;
        Toast.makeText(getApplicationContext(), "Pressed cancel"+proximity, Toast.LENGTH_SHORT).show();
    }


    // Location request adapted from https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
    protected void startLocationUpdates() {
        // Test message to see if location is updating
        Toast.makeText(getApplicationContext(), "updates", Toast.LENGTH_SHORT).show();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback(){
            // Change stored location when location changes
            @Override
            public void onLocationResult (LocationResult locationResult){
            onLocationChanged(locationResult.getLastLocation());
            }
        },
        Looper.myLooper());
    }

    // set up google map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng dublin = new LatLng(53.3498, -6.2603);
        //mMap.addMarker(new MarkerOptions().position(dublin).title("This is Dublin!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dublin, 10));

    }

    public void showBin() {
        LatLng bin1 = new LatLng(53, 6);
        mMap.addMarker(new MarkerOptions().position(bin1).title("This is a BIN!"));
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

}
