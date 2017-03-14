package com.nearby.whatsnearby.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.nearby.whatsnearby.services.AppController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Class to check GooglePlayServices on devices
 */
public class GooglePlayServicesUtil {
    private static int status;
    private static String msg = null;
    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;

    public static String getGooglePlayAvailabilityStatus(final Context context) {
        mActivity = AppController.getInstance().getCurrentActivity();
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

    public static void showGoogleDialog() {
        int requestCode = 10;
        Dialog googlePlayServicesDialog = com.google.android.gms.common.GooglePlayServicesUtil
                .getErrorDialog(status, mActivity, requestCode);
        googlePlayServicesDialog.show();
    }

    /**
     * Gets the admob test ads device id
     *
     * @param activity Activity - requires an activity instance to get the android id
     * @return String - returns the hashed device id
     */
    public static String getAdMobDeviceId(Activity activity) {
        String androidId = getDeviceId(activity);
        return getMessageDigest(androidId).toUpperCase();
    }

    /**
     * Gets device id.
     *
     * @param activity the activity
     * @return the device id
     */
    public static String getDeviceId(Activity activity) {
        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return deviceId;
    }

    /**
     * Gets the hashed value on android_id which will be useful for loading test ads
     *
     * @param android_id String - android id on which MD5 going to perform
     * @return String - hashed android id
     */
    private static String getMessageDigest(final String android_id) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(android_id.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e("GooglePlayServicesUtil", "Exception - " + e.getMessage());
        }
        return "";
    }
}
