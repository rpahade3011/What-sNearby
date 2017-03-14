package com.nearby.whatsnearby.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.GlobalSettings;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by rudhraksh.pahade on 9/15/2016.
 */

public class GpsStatusReceiver extends BroadcastReceiver {

    private Context context;
    private LocationManager locationManager = null;
    private NotificationManager notificationManager = null;

    @Override
    public void onReceive(Context ctext, Intent intent) {
        context = ctext;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                sendNotification();
            } else {
                if (notificationManager != null) {
                    notificationManager.cancel(GlobalSettings.GPS_NOTIFICATION_ID);
                }
            }
        }
    }

    private void sendNotification() {

        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, locationIntent, 0);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(context.getResources().getString(R.string.gps_broadcast_notification_msg))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.gps_broadcast_notification_msg)))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri);

        notificationManager.notify(GlobalSettings.GPS_NOTIFICATION_ID, notificationBuilder.build());
    }
}
