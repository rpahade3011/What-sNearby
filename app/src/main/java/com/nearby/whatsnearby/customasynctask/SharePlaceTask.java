package com.nearby.whatsnearby.customasynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nearby.whatsnearby.utilities.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SharePlaceTask {
    private double lat;
    private double lng;

    public SharePlaceTask(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    public Bitmap execute() {
        final String locationUrl = Utils.getInstance().getUrlForStaticMaps(lat, lng);
        InputStream is = null;
        try {
            is = new URL(locationUrl.trim()).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(is);
    }
}
