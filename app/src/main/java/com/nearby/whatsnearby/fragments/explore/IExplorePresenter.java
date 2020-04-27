package com.nearby.whatsnearby.fragments.explore;

import android.view.View;

public interface IExplorePresenter {
    void initializeViews(View view);
    void initializePlaces();
    void gotoPlace(View view, int placePosition);
}
