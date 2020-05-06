package com.nearby.whatsnearby.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
public class PermissionsUtil {
    private static final String TAG = "PermissionsUtil";
    private static PermissionsUtil mInstance = null;

    private PermissionsUtil() {}

    public static PermissionsUtil getInstance() {
        if (mInstance == null) {
            mInstance = new PermissionsUtil();
        }
        return mInstance;
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean ifPermissionsAreGranted(Activity activityContext) {
        WeakReference<Activity> activityWeakReference = new WeakReference<>(activityContext);
        Log.i(TAG, "Checking permissions");
        int permissionResult = ActivityCompat
                .checkSelfPermission(activityWeakReference.get().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionResult != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }
}