package com.nearby.whatsnearby.requests;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nearby.whatsnearby.AlertType;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.utilities.Utils;

public class NetworkTask {
    private static final String LOG_TAG = "NetworkTask";
    private static NetworkTask mInstance = null;

    private FetchFromServerUser user;
    private int id;

    private NetworkTask(FetchFromServerUser user, int id) {
        this.user = user;
        this.id = id;
        Log.d(LOG_TAG, "Id: " + id);
    }

    public static NetworkTask getInstance(FetchFromServerUser user, int id) {
        if (mInstance == null) {
            mInstance = new NetworkTask(user, id);
        }
        return mInstance;
    }

    public void executeNearbyPlacesTask(String url) {
        Log.i(LOG_TAG, "executeNearbyPlacesTask() URL --> " + url);
        user.onPreFetch(AlertType.DISCOVER_NEARBY_PLACES);
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, url, null, response1 -> {
                    Log.i(LOG_TAG, "executeNearbyPlacesTask() Response --> " + response1);
                    if (response1 != null) {
                        user.onFetchCompletion(response1.toString(), id,
                                AlertType.DISCOVER_NEARBY_PLACES);
                    }
                }, error -> {
                    VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void getPlaceDetails(String placeRef) {
        user.onPreFetch(AlertType.GET_PLACE_DETAILS);
        String url = Utils.getInstance().getPlaceDetailsUrl(placeRef);
        Log.i(LOG_TAG, "getPlaceDetails() URL --> " + url);
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET,
                        url,
                        null,
                        response -> {
                            if (response != null) {
                                user.onFetchCompletion(response.toString(), id,
                                        AlertType.GET_PLACE_DETAILS);
                            }
                        }, error -> {
                    VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void executeAutocompleteSearch(String url) {
        Log.i(LOG_TAG, "executeAutocompleteSearch() URL --> " + url);
        user.onPreFetch(AlertType.AUTO_COMPLETE_SEARCH);
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET,
                        url,
                        null,
                        response -> {
                            if (response != null) {
                                user.onFetchCompletion(response.toString(), id,
                                        AlertType.AUTO_COMPLETE_SEARCH);
                            }
                        }, error -> {

                });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void executeSearchedPlaceDetailTask(String url) {
        Log.i(LOG_TAG, "executeSearchedPlaceDetailTask() URL --> " + url);
        user.onPreFetch(AlertType.GET_PLACE_DETAILS);
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET,
                        url,
                        null,
                        response -> {
                            if (response != null) {
                                user.onFetchCompletion(response.toString(), id,
                                        AlertType.GET_PLACE_DETAILS);
                            }
                        }, error -> {

                });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void calculateDistanceBetweenTwoLocations(double srcLat,
                                                       double srcLng,
                                                       double destLat,
                                                       double destLng,
                                                       FetchFromServerUser user) {
        String distanceMatrixUrl = Utils.getInstance()
                .getDistanceMatrixUrl(srcLat, srcLng,
                        destLat, destLng);
        Log.i(LOG_TAG, "calculateDistanceBetweenTwoLocations() URL --> " + distanceMatrixUrl);
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET,
                        distanceMatrixUrl,
                        null,
                        response -> {
                            if (response != null) {
                                user.onFetchCompletion(response.toString(), id,
                                        AlertType.CALCULATE_DISTANCE_BETWEEN_TWO_LOCATIONS);
                            }
                        }, error -> {

                });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}