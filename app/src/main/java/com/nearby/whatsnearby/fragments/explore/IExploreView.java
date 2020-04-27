package com.nearby.whatsnearby.fragments.explore;

import android.view.View;

public interface IExploreView {
    void onNotifyUIReady(View view);
    void setUpPlaces();
    void navigateToPlace(View view, int placePosition);
}
