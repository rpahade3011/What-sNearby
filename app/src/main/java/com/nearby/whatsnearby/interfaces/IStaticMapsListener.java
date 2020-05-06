package com.nearby.whatsnearby.interfaces;

import android.graphics.Bitmap;

public interface IStaticMapsListener {
    void onStaticMapReceived(Bitmap mapImage);
}