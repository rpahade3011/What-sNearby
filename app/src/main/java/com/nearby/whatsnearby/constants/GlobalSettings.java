package com.nearby.whatsnearby.constants;

import android.os.Environment;

/**
 * Created by rudhraksh.pahade on 9/19/2016.
 */

public class GlobalSettings {

    public static final int GPS_NOTIFICATION_ID = 7;

    public static final String LOG_FILE_PATH
            = Environment.getExternalStorageDirectory() + "/" + "WhatsNearby/";

//    public static final String FONT_PATH = "fonts/ProductSans-Regular.ttf";
    public static final String FONT_PATH = "fonts/Metropolis-Regular.otf";
    /**
     * The default socket timeout in milliseconds
     */
    public static final int DEFAULT_TIMEOUT_MS = 60 * 1000;

    /**
     * The default number of retries
     */
    public static final int DEFAULT_MAX_RETRIES = 0;

    /**
     * The default backoff multiplier
     */
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    public static final String WHATS_NEARBY_GOOGLE_PLAY_STORE_URL_LINK
            = "https://play.google.com/store/apps/details?id=com.nearby.whatsnearby";

    public static long BACK_PRESSED;

    public static final String PREFIX_DEFAULT_GOOGLE_NAVIGATE = "google.navigation:q=";

    public static final int NAV_INDEX_HOME = 0;
    public static final int NAV_INDEX_EXPLORE = 1;
    public static final int NAV_INDEX_ABOUT = 2;
}