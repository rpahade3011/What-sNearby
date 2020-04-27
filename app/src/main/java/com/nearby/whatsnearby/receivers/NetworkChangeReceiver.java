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
    private static final String CONNECTION_TITLE = "Connected";
    private static final String CONNECTION_MESSAGE = "Great! Internet connection established";

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getInstance().getConnectivityStatusString(context);
        switch (status) {
            case "Not connected to internet":
                TopMessageManager.showError(NO_NET_MESSAGE, NO_NET_TITLE, TopMessage.DURATION.LONG);
                break;
            case "Wifi enabled":
            case "Mobile data enabled":
                TopMessageManager.showSuccess(CONNECTION_MESSAGE, CONNECTION_TITLE, TopMessage.DURATION.LONG);
                break;
        }
    }
}