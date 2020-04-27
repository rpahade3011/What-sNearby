package com.nearby.whatsnearby.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.utilities.NotificationHelper;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by rudraksh.pahade on 9/15/2016.
 */

public class GpsStatusReceiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context ctext, Intent intent) {
        context = ctext;
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            if (locationManager != null
                    && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                NotificationHelper.getInstance(context).showNotification();
            } else {
                if (NotificationHelper.getInstance(context).getNotificationManager() != null) {
                    NotificationHelper.getInstance(context).getNotificationManager()
                            .cancel(GlobalSettings.GPS_NOTIFICATION_ID);
                }
            }
        }
    }
}