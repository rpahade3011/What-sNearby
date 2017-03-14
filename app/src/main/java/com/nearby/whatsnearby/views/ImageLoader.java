package com.nearby.whatsnearby.views;

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

public class ImageLoader {

    String url;
    ImageView imageView;
    Context context;

    public ImageLoader(Context context, String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
        this.context = context;
    }

    public void loadThumbnailImage() throws Exception {
        try {
            new DownloadThumbImageTask().execute(url);
        } catch (Exception ex) {
            throw new Exception("Something went wrong on the server.");
        }
    }

    private class DownloadThumbImageTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            Log.d("doInBackground()", "Downloading Image");
            return getThumbURL(fetchNow(urls[0]));
        }

        protected void onPostExecute(String result) {
            Picasso.with(context).load(result).into(imageView);
        }

        public String fetchNow(String url) {
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

        public String getThumbURL(String data) {
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
}
