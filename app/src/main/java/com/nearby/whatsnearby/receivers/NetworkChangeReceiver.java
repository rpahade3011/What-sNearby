package com.nearby.whatsnearby.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nearby.whatsnearby.utilities.NetworkUtil;
import com.wizchen.topmessage.TopMessage;
import com.wizchen.topmessage.TopMessageManager;

/**
 * Created by rudraksh on 10/7/16.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String NO_NET_MESSAGE = "Err! Internet connection has been lost";
    private static final String NO_NET_TITLE = "Connecting...";

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        if (status.equals("Not connected to internet")) {
            TopMessageManager.showError(NO_NET_MESSAGE, NO_NET_TITLE, TopMessage.DURATION.LONG);
        }
    }
}
