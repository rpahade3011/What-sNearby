package com.nearby.whatsnearby.customasynctask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FetchFromServerTask extends AsyncTask<String, Void, String> {
    private FetchFromServerUser user;
    private int id;

    public FetchFromServerTask(FetchFromServerUser user, int id) {
        this.user = user;
        this.id = id;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        user.onPreFetch();
    }

    @Override
    protected String doInBackground(String... params) {

        URL urlCould;
        HttpURLConnection connection;
        InputStream inputStream = null;
        try {
            String url = params[0];
            urlCould = new URL(url);
            connection = (HttpURLConnection) urlCould.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestMethod("GET");
            connection.connect();

            inputStream = connection.getInputStream();

        } catch (MalformedURLException MEx) {

        } catch (IOException IOEx) {
            Log.e("Utils", "HTTP failed to fetch data");
            return null;
        }
        return Streams.readStream(inputStream);
    }

    protected void onPostExecute(String string) {
        user.onFetchCompletion(string, id);
    }
}