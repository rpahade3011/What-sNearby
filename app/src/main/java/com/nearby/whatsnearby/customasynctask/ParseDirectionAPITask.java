package com.nearby.whatsnearby.customasynctask;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nearby.whatsnearby.places.DirectionsJSONParser;
import com.nearby.whatsnearby.utilities.MapUtil;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rudhraksh.pahade on 13-07-2016.
 */

public class ParseDirectionAPITask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    private WeakReference<Activity> activity;
    private GoogleMap map;
    private Circle mCircle = null;

    public ParseDirectionAPITask(Activity a, GoogleMap gm) {
        this.activity = new WeakReference<>(a);
        this.map = gm;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            // Starts parsing data
            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }
    // Executes in UI thread, after the parsing process

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        super.onPostExecute(result);
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(14);
            lineOptions.color(Color.parseColor("#444153"));

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(MapUtil.getInstance().getSourceBounds());
            boundsBuilder.include(MapUtil.getInstance().getDestinationBounds());
            LatLngBounds bounds = boundsBuilder.build();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            activity.get().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels - 100;
            int width = displaymetrics.widthPixels;
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height - width, 100));

            // Start destination location ripple
            makeDestinationRipple();
        }
    }

    private void makeDestinationRipple() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mCircle = map.addCircle(new CircleOptions()
                        .center(MapUtil.getInstance().getDestinationBounds()).radius(500)
                        .strokeColor(1)
                        .strokeColor(0x5530d1d5)
                        .fillColor(0x55383547));
            }
        });
    }
}
