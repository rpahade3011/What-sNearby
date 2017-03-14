package com.nearby.whatsnearby.utilities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.services.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static LatLng sourceBounds;
    public static LatLng destinationBounds;

    public static String getDirectionUrl(LatLng source, LatLng destination) {
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
                AppController.getInstance().getResources().getString(R.string.google_places_search_server_key);

        return url;
    }

    @SuppressLint("LongLogTag")
    public static String getMapStreams(String strUrl) throws IOException {
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

    public static void calculateNearbyDistance (final LatLng srcLatLng, final LatLng destLatLng) {
        final String distanceMatrixUrlApiKey = AppController.getInstance().getResources().getString(R.string.google_places_search_server_key);
        new AsyncTask<Void, Void, Void>() {
            String data = null;

            @Override
            protected Void doInBackground(Void... params) {
                String distanceMatrixUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                        + srcLatLng.latitude + "," + srcLatLng.longitude + "&destinations=" + destLatLng.latitude + ","
                        + destLatLng.longitude + "&mode=driving&language=en&key=" + distanceMatrixUrlApiKey;
                try {
                    data = getMapStreams(distanceMatrixUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (data != null) {
                    Log.e("MapUtils", "calculateDistance().onPostExecute() - " + data);
                    try {
                        JSONObject resultJson = new JSONObject(data);

                        JSONArray rowArray = resultJson.getJSONArray("rows");
                        JSONObject jsonObject = rowArray.getJSONObject(0);

                        JSONArray elementArray = jsonObject.getJSONArray("elements");

                        JSONObject finalObject = elementArray.getJSONObject(0);

                        JSONObject durationObject = finalObject.getJSONObject("duration");

                        String durationText = durationObject.optString("text");
                        int durationValue = durationObject.getInt("value");

                        GlobalSettings.DISTANCE_AND_TIME_ETA = durationText;

                        JSONObject distanceObject = finalObject.getJSONObject("distance");

                        String dText = distanceObject.optString("text");
                        int distanceValue = distanceObject.getInt("value");
                        double roundOff = Math.round((distanceValue / 1609.34) * 100.0) / 100.0;

                        String distanceText = dText.replace(",", ".");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
