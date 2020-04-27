package com.nearby.whatsnearby.fragments.home;

import android.view.View;

public class HomePresenter implements IHomePresenter {

    private HomeView mHomeView;

    public HomePresenter(HomeView view) {
        this.mHomeView = view;
    }

    @Override
    public void setup(View view) {
        if (mHomeView != null) {
            mHomeView.notifyUIReady(view);
        }
    }

    @Override
    public void initializeAds(View view) {
        if (mHomeView != null) {
            mHomeView.setupAds(view);
        }
    }
}
