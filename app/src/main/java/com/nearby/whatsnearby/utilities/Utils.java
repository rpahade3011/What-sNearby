package com.nearby.whatsnearby.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.services.AppController;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by rudhraksh.pahade on 25-07-2016.
 */

public class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();
    private static Utils mInstance = null;

    private Utils() {}

    public static Utils getInstance() {
        if (mInstance == null) {
            mInstance = new Utils();
        }
        return mInstance;
    }

    /**
     * Retrieve your own app version
     *
     * @param context Context
     * @return String with the app version
     */
    public String getAppVersionName(Context context) {
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
    public int getAppVersionCode(Context context) {
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
    public void goToGooglePlay(Context context, String id) {
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
    public void goToGooglePlus(Context context, String id) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/" + id)));
    }

    /**
     * OPENS FACEBOOK ACCOUNT
     *
     * @param context
     * @param id
     */

    public Intent getFacebookIntent(Context context, String id) {
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
    public Intent getTwitterIntent(Context context, String id) {
        try {
            context.getPackageManager().getPackageInfo("com.twitter.android", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=" + id));
        } catch (PackageManager.NameNotFoundException nnfe) {
            nnfe.getMessage();
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/" + id));
        }
    }

    public boolean createDir(String LOG_DIR) {
        File file = new File(LOG_DIR);
        if (!file.exists()) {
            try {
                file.mkdirs();
                Log.i(LOG_TAG, "DIRECTORY CREATED, LOG_DIR : " + LOG_DIR);
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
    private String getDeviceId(Activity activity) {
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
    public String getAdMobDeviceId(Activity activity) {
        String androidId = getDeviceId(activity);
        return getMessageDigest(androidId).toUpperCase();
    }


    /**
     * Gets the hashed value on android_id which will be useful for loading test ads
     *
     * @param android_id String - android id on which MD5 going to perform
     * @return String - hashed android id
     */
    private String getMessageDigest(final String android_id) {
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

    public String getNearbySearchUrl(String placeId, double lat, double lng) {
        StringBuilder stringBuilder =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
        stringBuilder.append(lat);
        stringBuilder.append(",");
        stringBuilder.append(lng);
        stringBuilder.append("&rankby=distance&types=");
        stringBuilder.append(placeId);
        stringBuilder.append("&key=");
        stringBuilder.append(AppController.getInstance()
                .getApplicationContext().getResources().getString(R.string.google_maps_key));
        return stringBuilder.toString();
    }

    public String getPlaceDetailsUrl(String placeRef) {
        StringBuilder stringBuilder =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?placeid=");
        stringBuilder.append(placeRef);
        stringBuilder.append("&key=");
        stringBuilder.append(AppController.getInstance()
                .getApplicationContext().getResources().getString(R.string.google_maps_key));
        return stringBuilder.toString();
    }

    public String getUrlForStaticMaps(double lat, double lng) {
        StringBuilder locationBuilder = new StringBuilder();
        locationBuilder.append(AppController.getInstance()
                .getApplicationContext().getResources().getString(R.string.google_map_static_api_url));
        locationBuilder.append(lat);
        locationBuilder.append(",");
        locationBuilder.append(lng);
        locationBuilder.append("&zoom=13&size=100x100&scale=2&format=jpeg&maptype=roadmap");
        locationBuilder.append("&markers=color:blue");
        locationBuilder.append("|");
        locationBuilder.append("label:");
        locationBuilder.append("|");
        locationBuilder.append(lat);
        locationBuilder.append(",");
        locationBuilder.append(lng);
        locationBuilder.append("&key=");
        locationBuilder.append(AppController.getInstance()
                .getApplicationContext().getResources().getString(R.string.google_maps_key));

        return locationBuilder.toString();
    }

    public String getDistanceMatrixUrl(double srcLat, double srcLng, double destLat, double destLng) {
        StringBuilder matrixUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?origins=");
        matrixUrl.append(srcLat);
        matrixUrl.append(",");
        matrixUrl.append(srcLng);
        matrixUrl.append("&destinations=");
        matrixUrl.append(destLat);
        matrixUrl.append(",");
        matrixUrl.append(destLng);
        matrixUrl.append("&mode=driving&language=en&key=");
        matrixUrl.append(AppController.getInstance().getApplicationContext()
                .getResources().getString(R.string.google_maps_key));

        return matrixUrl.toString();
    }

    public String getSearchUrl(String place, double latitude, double longitude) {
        final String GOOGLE_PLACES_URL = "maps.googleapis.com/maps/api/place/autocomplete/json";
        final int SEARCH_RADIUS = 1000;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .encodedAuthority(GOOGLE_PLACES_URL)
                .appendQueryParameter("input", place)
                .appendQueryParameter("location", latitude + "," + longitude)
                .appendQueryParameter("radius", String.valueOf(SEARCH_RADIUS))
                .appendQueryParameter("key", AppController.getInstance()
                        .getApplicationContext().getResources().getString(R.string.google_maps_key));
        return builder.build().toString();
    }

    public String getSearchedPlaceDetailsUrl(String placeId) {
        StringBuilder stringBuilder =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?placeid=");
        stringBuilder.append(placeId);
        stringBuilder.append("&key=");
        stringBuilder.append(AppController.getInstance()
                .getApplicationContext().getResources().getString(R.string.google_maps_key));
        return stringBuilder.toString();
    }

    public String getPlaceImagesUrl(Context context, String imageRef) {
        StringBuilder stringBuilder =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=");
        stringBuilder.append(imageRef);
        stringBuilder.append("&key=");
        stringBuilder.append(context.getResources().getString(R.string.google_maps_key));
        return stringBuilder.toString();
    }

    /**
     * Checks if the device has Android OS version "8.0" "Oreo" or later version.
     * @return
     */
    public boolean isOreoOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}