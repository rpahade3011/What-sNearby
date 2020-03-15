package com.nearby.whatsnearby.customasynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class DownloadThumbImageTask extends AsyncTask<String, Void, String> {
    private Context mContext;
    private ImageView mImageView;

    public DownloadThumbImageTask(Context ctx, ImageView imgView) {
        this.mContext = ctx;
        this.mImageView = imgView;
    }

    @Override
    protected String doInBackground(String... urls) {
        Log.d("doInBackground()", "Downloading Image");
        return getThumbURL(fetchNow(urls[0]));
    }

    @Override
    protected void onPostExecute(String s) {
        Picasso.with(mContext).load(s).into(mImageView);
    }

    private String fetchNow(String url) {
        HttpURLConnection connection = null;
        BufferedReader buffer = null;
        StringBuffer stringBuffer = new StringBuffer();

        try {
            URL cloudURL = new URL(url);

            connection = (HttpURLConnection) cloudURL.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream stream = connection.getInputStream();
            buffer = new BufferedReader(new InputStreamReader(stream));
            String line;

            while ((line = buffer.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }

        } catch (SocketTimeoutException ex) {
            Log.e("ImageLoader", "Socket connection timeout", ex);
        } catch (FileNotFoundException ex) {
            Log.e("ERROR", url);
        } catch (Exception ex) {
            Log.e("ERROR", "Unknown Error", ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException ex) {
                    Log.e("ImageLoader", "Error Closing Stream", ex);
                }
            }
        }
        return stringBuffer.toString();
    }

    private String getThumbURL(String data) {
        String result = null;
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("image")) {
                String[] temp = jsonObject.getJSONObject("image").getString("url").split("=");
                result = temp[0] + "=100";
            }
        } catch (JSONException ex) {
            Log.e("Review Adapter", ex.getMessage());
        }
        return result;
    }
}
