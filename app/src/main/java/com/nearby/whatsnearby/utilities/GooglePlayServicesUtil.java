package com.nearby.whatsnearby.utilities;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.nearby.whatsnearby.services.AppController;

import java.lang.ref.WeakReference;


/**
 * Class to check GooglePlayServices on devices
 */
public class GooglePlayServicesUtil {
    private static GooglePlayServicesUtil mInstance = null;
    private int status;
    private String msg = null;
    private WeakReference<Activity> mActivity;

    private GooglePlayServicesUtil() {}

    public static GooglePlayServicesUtil getInstance() {
        if (mInstance == null) {
            mInstance = new GooglePlayServicesUtil();
        }
        return mInstance;
    }

    public String getGooglePlayAvailabilityStatus(final Context context) {
        mActivity = new WeakReference<>(AppController.getInstance().getCurrentActivity());
        // Getting Google Play availability status
        status = com.google.android.gms.common.GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);
        // Showing status
        // Google Play Services are not available
        if (status != ConnectionResult.SUCCESS) {
            msg = "Google play services are not available";
        }
        return msg = "Google play services are available";
    }
}