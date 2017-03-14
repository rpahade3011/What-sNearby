package com.nearby.whatsnearby.customasynctask;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.nearby.whatsnearby.utilities.MapUtil;


/**
 * Created by rudhraksh.pahade on 13-07-2016.
 */

public class ExecuteDirectionsAPI extends AsyncTask<String, Void, String> {

    private Activity activity;
    private GoogleMap map;

    public ExecuteDirectionsAPI(Activity a, GoogleMap gm) {
        this.activity = a;
        this.map = gm;
    }

    // Downloading data in non-ui thread
    @Override
    protected String doInBackground(String... url) {
        // For storing data from web service
        String data = "";
        try {
            Log.d("URL for direction", url[0]);
            // Fetching the data from web service
            data = MapUtil.getMapStreams(url[0]);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }
    // Executes in UI thread, after the execution of
    // doInBackground()

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        ParseDirectionAPITask parseDirectionAPITask = new ParseDirectionAPITask(activity, map);
        // Invokes the thread for parsing the JSON data
        parseDirectionAPITask.execute(s);
    }
}
