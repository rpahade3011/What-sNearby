package com.nearby.whatsnearby.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by rudhraksh.pahade on 25-07-2016.
 */

public class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();
    /**
     * Retrieve your own app version
     *
     * @param context Context
     * @return String with the app version
     */
    public static String getAppVersionName(Context context) {
        String res = "0.0.0.0";
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Retrieve your own app version code
     *
     * @param context Context
     * @return int with the app version code
     */
    public static int getAppVersionCode(Context context) {
        int res = 0;
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Opens Google Play if installed, if not opens browser
     *
     * @param context Context
     * @param id      PackageName on Google Play
     */
    public static void goToGooglePlay(Context context, String id) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + id)));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + id)));
        }
    }

    /**
     * Opens Google Plus
     *
     * @param context Context
     * @param id      Name on Google Play
     */
    public static void goToGooglePlus(Context context, String id) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/" + id)));
    }

    /**
     * OPENS FACEBOOK ACCOUNT
     *
     * @param context
     * @param id
     */

    public static Intent getFacebookIntent(Context context, String id) {
        try {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + id));
        } catch (PackageManager.NameNotFoundException nnfe) {
            nnfe.getMessage();
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + id));
        }
    }

    /**
     * OPENS TWITTER ACCOUNT
     *
     * @param context
     * @param id
     */
    /*public static void goToTwitter(Context context, String id){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/" + id)));
    }*/
    public static Intent getTwitterIntent(Context context, String id) {
        try {
            context.getPackageManager().getPackageInfo("com.twitter.android", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=" + id));
        } catch (PackageManager.NameNotFoundException nnfe) {
            nnfe.getMessage();
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/" + id));
        }
    }

    public static boolean createDir(String LOG_DIR) {
        File file = new File(LOG_DIR);
        if (!file.exists()) {
            try {
                file.mkdirs();
                Log.e(LOG_TAG, "DIRECTORY CREATED, LOG_DIR : " + LOG_DIR);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
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
            Log.e("Utils", "Exception - " + e.getMessage());
        }
        return "";
    }
}
