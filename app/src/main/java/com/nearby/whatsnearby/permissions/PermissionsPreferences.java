package com.nearby.whatsnearby.permissions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rudhraksh.pahade on 12-07-2016.
 */

public class PermissionsPreferences {
    private static final String PREF_LOCATION_PERM = "prefLocationPermission";
    private static final String PREF_EXTERNAL_PERM = "prefExternalStorage";
    private static final String PREF_CALL_PHONE = "prefCallPhone";
    private static final String permissionSharedPrefName = "permissionSharedPrefName";
    private static final String PREF_CHECKED_PERMISSIONS = "prefCheckedPermissions";

    private SharedPreferences spPermissions = null;
    private SharedPreferences.Editor spEditor = null;

    @SuppressLint("CommitPrefEdits")
    public boolean savePermissionPreferences(Context context, boolean isLocationPermission,
                                             boolean isExternalPermission, boolean isCallPhone) {
        if (isLocationPermission && isExternalPermission && isCallPhone) {
            spPermissions = context.getSharedPreferences(permissionSharedPrefName, Context.MODE_PRIVATE);
            spEditor = spPermissions.edit();
            spEditor.putBoolean(PREF_LOCATION_PERM, true);
            spEditor.putBoolean(PREF_EXTERNAL_PERM, true);
            spEditor.putBoolean(PREF_CALL_PHONE, true);
            spEditor.commit();
            return true;
        }
        return false;
    }

    public boolean getPermissionPreferences(Context context) {
        spPermissions = context.getSharedPreferences(permissionSharedPrefName, Context.MODE_PRIVATE);
        boolean isLocationPermission = spPermissions.getBoolean(PREF_LOCATION_PERM, false);
        boolean isExternalPermission = spPermissions.getBoolean(PREF_EXTERNAL_PERM, false);
        boolean isCallPhone = spPermissions.getBoolean(PREF_EXTERNAL_PERM, false);
        return isLocationPermission && isExternalPermission && isCallPhone;
    }


    @SuppressLint("CommitPrefEdits")
    public boolean setApplicationOk(Context context, boolean isOk) {
        if (isOk) {
            spPermissions = context.getSharedPreferences(permissionSharedPrefName, Context.MODE_PRIVATE);
            boolean isLocationPermission = spPermissions.getBoolean(PREF_LOCATION_PERM, false);
            boolean isExternalPermission = spPermissions.getBoolean(PREF_EXTERNAL_PERM, false);
            boolean isCallPhone = spPermissions.getBoolean(PREF_EXTERNAL_PERM, false);
            if (isLocationPermission && isExternalPermission && isCallPhone) {
                spEditor = spPermissions.edit();
                spEditor.putBoolean(PREF_CHECKED_PERMISSIONS, true);
                return true;
            }

        }
        return false;
    }

    public boolean getApplicationOk(Context context) {
        spPermissions = context.getSharedPreferences(permissionSharedPrefName, Context.MODE_PRIVATE);
        return spPermissions.getBoolean(PREF_CHECKED_PERMISSIONS, false);
    }
}
