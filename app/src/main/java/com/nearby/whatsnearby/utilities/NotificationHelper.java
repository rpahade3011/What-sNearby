package com.nearby.whatsnearby.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.GlobalSettings;

public class NotificationHelper {
    private static final String LOG_TAG = "NotificationHelper";
    private static final String NOTIFICATION_ID_OREO = "40007";
    private static final String NOTIFICATION_CHANNEL_NAME = "WhatsNearby_Channel";
    private static NotificationHelper mInstance = null;
    private Context mContext;
    private NotificationManager mNotificationManager = null;

    public static NotificationHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotificationHelper(context);
        }
        return mInstance;
    }

    private NotificationHelper(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    public void showNotification() {
        Log.i(LOG_TAG, "Notification for GPS off is displayed");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*Creates an explicit intent for an Activity in your app*/
            // Supporting notifications for older versions
            Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, locationIntent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle(mContext.getResources().getString(R.string.app_name));
            mBuilder.setContentText(mContext.getResources()
                    .getString(R.string.gps_broadcast_notification_msg));
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(mContext.getResources()
                            .getString(R.string.gps_broadcast_notification_msg)));
            mBuilder.setAutoCancel(true);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // Notification channel
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID_OREO,
                    NOTIFICATION_CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            mBuilder.setChannelId(NOTIFICATION_ID_OREO);
            mNotificationManager.createNotificationChannel(notificationChannel);

            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
        } else {
            // Supporting notifications for older versions
            Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, locationIntent, 0);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(mContext.getResources().getString(R.string.app_name))
                    .setContentText(mContext.getResources().getString(R.string.gps_broadcast_notification_msg))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(mContext.getResources().getString(R.string.gps_broadcast_notification_msg)))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSound(defaultSoundUri);

            mNotificationManager.notify(GlobalSettings.GPS_NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}