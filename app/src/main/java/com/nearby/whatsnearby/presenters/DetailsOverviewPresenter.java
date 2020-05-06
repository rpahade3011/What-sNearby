package com.nearby.whatsnearby.presenters;

import com.nearby.whatsnearby.interfaces.IDetailsOverviewPresenter;
import com.nearby.whatsnearby.interfaces.IDetailsOverviewView;

public class DetailsOverviewPresenter implements IDetailsOverviewPresenter {
    private IDetailsOverviewView mView;

    public DetailsOverviewPresenter(IDetailsOverviewView view) {
        this.mView = view;
    }

    @Override
    public void getIntentData() {
        mView.getIntentData();
    }

    @Override
    public void notifyUIReady() {
        mView.notifyUIReady();
    }

    @Override
    public void setupViewPager() {
        mView.setupViewPager();
    }

    @Override
    public void setupTabs() {
        mView.setupTabs();
    }
}