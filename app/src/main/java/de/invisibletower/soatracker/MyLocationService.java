package de.invisibletower.soatracker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by daniel on 21.03.17.
 */

public class MyLocationService implements LocationListener {

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private final static boolean forceNetwork = false;

    private static MyLocationService instance = null;

    private LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;

    public boolean isGPSEnabled;
    public boolean isNetworkEnabled;
    public boolean locationServiceAvailable;

    Runnable onLocation;

    /**
     * Singleton implementation
     * @return
     */
    public static MyLocationService getLocationManager(Context context)     {
        if (instance == null) {
            instance = new MyLocationService(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    private MyLocationService( Context context )     {
        initLocationService(context);
    }



    /**
     * Sets up location service after permissions is granted
     */
    @TargetApi(23)
    private void initLocationService(Context context) {


        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("MARKER", "Permission Loc Failed");

            return;
        }

        try {
            this.longitude = 0.0;
            this.latitude = 0.0;
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (forceNetwork) isGPSEnabled = false;

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }
            //else
            {
                this.locationServiceAvailable = true;
            }
        } catch (Exception ex) {
            Log.e("MARKER", "Error creating location service: " + ex.getMessage());

        }

    }

    public void startLocating(Context context, Runnable onLocation) {
        this.onLocation = onLocation;
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("MARKER", "Permission fail 1");

            return;
        }
        Log.i("MARKER", "StartLocating()");
        try{
            if (locationServiceAvailable) {
                if (isNetworkEnabled) {
                    if (locationManager != null)   {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.i("MARKER", "Start Network Loc service");
                        //location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        //updateCoordinates();
                    }
                }

                if (isGPSEnabled)  {
                    if (locationManager != null)  {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.i("MARKER", "Start GPS Loc service");
                        //location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        //updateCoordinates();
                    }
                }
            }
        } catch (Exception ex)  {
            Log.e("MARKER", "Error creating location service: " + ex.getMessage() );

        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onLocationChanged(Location location)     {
        this.location = location;
        updateCoordinates();
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void updateCoordinates() {
        // location
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.i("MARKER", "Got new Loc: " + String.valueOf(latitude) + " " + String.valueOf(longitude) + " " + String.valueOf(location.getTime()));
        onLocation.run();
    }

    public void stop(Context context) {
        Log.i("MARKER", "Stop Loc service");
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        locationManager.removeUpdates(this);
    }
}