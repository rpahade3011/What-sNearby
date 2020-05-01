package com.nearby.whatsnearby.customasynctask;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.places.DirectionsJSONParser;
import com.nearby.whatsnearby.utilities.MapUtil;
import com.nearby.whatsnearby.views.MapAnimator;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by rudraksh.pahade on 13-07-2016.
 */

public class ParseDirectionAPITask extends AsyncTask<String, Integer,
        List<List<HashMap<String, String>>>> {

    private WeakReference<Activity> activity;
    private GoogleMap map;
    private ArrayList<LatLng> points = null;
    private List<LatLng> listLatLng = new ArrayList<>();

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
        drawPolyLine(result);
    }

    /**
     * Parses the directions.
     *
     * @param result
     */
    private void drawPolyLine(List<List<HashMap<String, String>>> result) {
        // // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();

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

            this.listLatLng.addAll(points);
        }
        startAnimatingLines();
    }

    /**
     * Starts animating source and destination routes.
     */
    private void startAnimatingLines() {
        addMarker(listLatLng.get(listLatLng.size() - 1));
        addOverlay(listLatLng.get(listLatLng.size() - 1));
        MapAnimator.getInstance().animateRoute(map, listLatLng);
    }

    /**
     * Adds a location marker at the destination.
     *
     * @param destination
     */
    private void addMarker(LatLng destination) {
        MarkerOptions options = new MarkerOptions();
        options.position(destination);
        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.flag_marker));
        map.addMarker(options);
    }

    /**
     * Makes a ripple effect at the destination end.
     *
     * @param place
     */
    private void addOverlay(LatLng place) {
        GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
                .position(place, 100)
                .transparency(0.5f)
                .zIndex(3)
                .image(BitmapDescriptorFactory.fromBitmap(MapUtil.getInstance()
                        .drawableToBitmap(activity.get().getApplicationContext()
                                .getDrawable(R.drawable.map_overlay)))));
        startOverlayAnimation(groundOverlay);
    }

    private void startOverlayAnimation(final GroundOverlay groundOverlay) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator vAnimator = ValueAnimator.ofInt(0, 100);
        vAnimator.setRepeatCount(ValueAnimator.INFINITE);
        vAnimator.setRepeatMode(ValueAnimator.RESTART);
        vAnimator.setInterpolator(new LinearInterpolator());
        vAnimator.addUpdateListener(animation -> {
            final Integer val = (Integer) animation.getAnimatedValue();
            groundOverlay.setDimensions(val);
        });

        ValueAnimator tAnimator = ValueAnimator.ofFloat(0, 1);
        tAnimator.setRepeatCount(ValueAnimator.INFINITE);
        tAnimator.setRepeatMode(ValueAnimator.RESTART);
        tAnimator.setInterpolator(new LinearInterpolator());
        tAnimator.addUpdateListener(animation -> {
            Float val = (Float) animation.getAnimatedValue();
            groundOverlay.setTransparency(val);
        });

        animatorSet.setDuration(3000);
        animatorSet.playTogether(vAnimator, tAnimator);
        animatorSet.start();
    }
}