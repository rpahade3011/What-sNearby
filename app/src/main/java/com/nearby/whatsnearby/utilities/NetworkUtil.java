package com.nearby.whatsnearby.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class to check device network connectivity and notifies Broadcast
 */
public class NetworkUtil {
    private static NetworkUtil mInstance = null;
    private final int TYPE_WIFI = 1;
    private final int TYPE_MOBILE = 2;
    private final int TYPE_NOT_CONNECTED = 0;

    private NetworkUtil() {}

    public static NetworkUtil getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkUtil();
        }
        return mInstance;
    }

    private int getConnectivityStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
            }
        }
        return TYPE_NOT_CONNECTED;
    }

    public String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = null;
        switch (conn) {
            case TYPE_WIFI:
                status = "Wifi enabled";
                break;
            case TYPE_MOBILE:
                status = "Mobile data enabled";
                break;
            case TYPE_NOT_CONNECTED:
                status = "Not connected to internet";
                break;
        }
        return status;
    }
}