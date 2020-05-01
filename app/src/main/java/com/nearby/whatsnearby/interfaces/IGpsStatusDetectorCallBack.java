package com.nearby.whatsnearby.interfaces;

public interface IGpsStatusDetectorCallBack {

    void onGpsSettingStatus(boolean enabled);

    void onGpsAlertCanceledByUser();
}