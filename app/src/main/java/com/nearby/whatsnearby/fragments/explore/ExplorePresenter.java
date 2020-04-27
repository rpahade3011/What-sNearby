package com.nearby.whatsnearby.fragments.explore;

import android.view.View;

public class ExplorePresenter implements IExplorePresenter {
    private IExploreView mExploreView;

    public ExplorePresenter(IExploreView exploreView) {
        this.mExploreView = exploreView;
    }

    @Override
    public void initializeViews(View view) {
        if (mExploreView != null) {
            mExploreView.onNotifyUIReady(view);
        }
    }

    @Override
    public void initializePlaces() {
        if (mExploreView != null) {
            mExploreView.setUpPlaces();
        }
    }

    @Override
    public void gotoPlace(View view, int placePosition) {
        if (mExploreView != null) {
            mExploreView.navigateToPlace(view, placePosition);
        }
    }
}