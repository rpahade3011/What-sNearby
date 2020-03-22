package com.nearby.whatsnearby.fragments.home;

import android.view.View;

public interface HomeView {
    void notifyUIReady(View rootView);
    void setupAds(View rootView);
}