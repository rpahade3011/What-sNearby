package com.nearby.whatsnearby.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.activities.ActivityBottomNavigationView;
import com.nearby.whatsnearby.receivers.GpsStatusReceiver;
import com.nearby.whatsnearby.receivers.NetworkChangeReceiver;
import com.nearby.whatsnearby.utilities.Utils;

public class MonitorService extends Service {
    private static final String TAG = MonitorService.class.getSimpleName();

    private GpsStatusReceiver gpsStatusReceiver = null;
    private NetworkChangeReceiver networkChangeReceiver = null;

    private static final String NOTIFICATION_ID_OREO = "40007";
    private static final String NOTIFICATION_CHANNEL_NAME = "WhatsNearby_Channel";
    private static final String ACTION_STOP_SERVICE = "Stop Monitor Service";
    private NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onHandleIntent() starting broadcast");
        if (Utils.getInstance().isOreoOrLater()) {
            if (intent.getAction() != null
                    && intent.getAction().equals(ACTION_STOP_SERVICE)) {
                manager.cancelAll();
                unRegisterAllBroadcast();
                stopForeground(true);
            } else {
                String inputExtra = intent.getStringExtra("inputExtra");

                createNotificationChannel();

                Intent notificationIntent = new Intent(this,
                        ActivityBottomNavigationView.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, 0);

                Intent stopSelf = new Intent(this, MonitorService.class);
                stopSelf.setAction(ACTION_STOP_SERVICE);

                PendingIntent stopServiceIntent = PendingIntent.getService(this,
                        0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

                Notification notification = new NotificationCompat.Builder(this,
                        NOTIFICATION_ID_OREO)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(inputExtra)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_oreo_noti_stop_black_24dp,
                                "Stop", stopServiceIntent)
                        .build();
                startForeground(47, notification);
            }
            registerAllBroadcasts();
        } else {
            registerAllBroadcasts();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterAllBroadcast();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerGpsStatusReceiver() {
        IntentFilter gpsIntentFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        // Registering broadcast receiver for GpsStatusChange
        if (gpsStatusReceiver == null) {
            gpsStatusReceiver = new GpsStatusReceiver();
            try {
                registerReceiver(gpsStatusReceiver, gpsIntentFilter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerNetworkChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        if (networkChangeReceiver == null) {
            networkChangeReceiver = new NetworkChangeReceiver();
            try {
                registerReceiver(networkChangeReceiver, intentFilter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unRegisterGpsStatusReceiver() {
        // Un registering broadcast receiver for GpsStatusChanges
        if (gpsStatusReceiver != null) {
            unregisterReceiver(gpsStatusReceiver);
        }
    }

    private void unRegisterNetworkChangeReceiver() {
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    private void registerAllBroadcasts() {
        registerGpsStatusReceiver();
        registerNetworkChangeReceiver();
    }

    private void unRegisterAllBroadcast() {
        unRegisterGpsStatusReceiver();
        unRegisterNetworkChangeReceiver();
    }

    /**
     * Creates a notification channel on Android OS version 8.0 or later.
     */
    private void createNotificationChannel() {
        if (Utils.getInstance().isOreoOrLater()) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_ID_OREO,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}