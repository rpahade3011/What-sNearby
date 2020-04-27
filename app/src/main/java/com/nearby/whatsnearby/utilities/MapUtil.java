package com.nearby.whatsnearby.utilities;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.requests.NetworkTask;
import com.nearby.whatsnearby.services.AppController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by rudhraksh.pahade on 13-07-2016.
 */

public class MapUtil {
    private static MapUtil mInstance = null;

    private LatLng mSourceBounds;
    private LatLng mDestinationBounds;
    private MutableLiveData<String> mDistanceAndTimeETA = new MutableLiveData<>();

    private MapUtil() {}

    public static MapUtil getInstance() {
        if (mInstance == null) {
            mInstance = new MapUtil();
        }
        return mInstance;
    }

    public LatLng getSourceBounds() {
        return mSourceBounds;
    }

    public void setSourceBounds(LatLng sourceBounds) {
        mSourceBounds = sourceBounds;
    }

    public LatLng getDestinationBounds() {
        return mDestinationBounds;
    }

    public void setDestinationBounds(LatLng destinationBounds) {
        mDestinationBounds = destinationBounds;
    }

    public LiveData<String> getDistanceTimeETA() {
        return mDistanceAndTimeETA;
    }

    public void setDistanceAndTimeETA(String eta) {
        mDistanceAndTimeETA.setValue(eta);
    }

    public String getDirectionUrl(LatLng source, LatLng destination) {
        // Origin of route
        String str_origin = "origin=" + source.latitude + "," + source.longitude;

        // Destination of route
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;

        // Sensor enabled
        String sensor = "sensor=true&alternatives=true";

        // Setting Mode for Driving
        String mode = "driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "mode=" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" +
                AppController.getInstance().getResources().getString(R.string.google_maps_key);

        return url;
    }

    @SuppressLint("LongLogTag")
    public String getMapStreams(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void calculateNearbyDistance (FetchFromServerUser user,
                                         final LatLng srcLatLng,
                                         final LatLng destLatLng) {
        NetworkTask.getInstance(0).calculateDistanceBetweenTwoLocations(srcLatLng.latitude,
                srcLatLng.longitude, destLatLng.latitude, destLatLng.longitude, user);
    }
}