package com.nearby.whatsnearby.services;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.nearby.whatsnearby.R;


/**
 * Created by rudraksh on 10/7/16.
 */
public class GpsTracker extends Service implements LocationListener {
    private final Context mContext;

    // Is that a GPS -enabled device?
    boolean isGPSEnabled = false;

    // Is the data connection on the device active ?
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    // Location
    Location location;
    // Latitude
    public double latitude;
    // Longitude
    public double longitude;

    // My position changed to require a minimum amount
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // metre

    // The location will require a minimum amount
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // dakika

    // LocationManager object
    protected LocationManager locationManager;

    /**
     * Constructor
     *
     * @param context
     */
    public GpsTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * Freeze the location information
     *
     * @return
     */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // Did you open the GPS ?
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Did you open the Internet ?
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                showSettingsAlert();
            } else {
                this.canGetLocation = true;

                // Once the location information received from the Internet are recorded
                if (isNetworkEnabled) {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    if (locationManager != null) {
                        try {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // The location information received from GPS
                if (isGPSEnabled) {
                    if (location == null) {
                        try {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        if (locationManager != null) {
                            try {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    // Gets Latitude
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    // Gets Longitude
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    // Location information is turned off while a message is displayed to the user containing a link to the settings page
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Title
        alertDialog.setTitle(mContext.getResources().getString(R.string.gps_disabled_alert_title));

        // Messgage
        alertDialog.setMessage(mContext.getResources().getString(R.string.gps_disabled_alert_msg));

        // Message icon
        //alertDialog.setIcon(R.drawable.delete);

        // Settings button is clicked in
        alertDialog.setPositiveButton(getResources().getString(R.string.gps_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // Cancel button is clicked in
        alertDialog.setNegativeButton(getResources().getString(R.string.gps_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Will show the message box
        alertDialog.show();
    }

    // LocationManager'in gps Stop requests
    public void stopUsingGPS() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(GpsTracker.this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
