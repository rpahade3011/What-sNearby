package com.nearby.whatsnearby.presenters;

import androidx.fragment.app.Fragment;

import com.nearby.whatsnearby.interfaces.IBottomNavPresenter;
import com.nearby.whatsnearby.interfaces.IBottomNavView;

public class BottomNavPresenter implements IBottomNavPresenter {
    private IBottomNavView mView;

    public BottomNavPresenter(IBottomNavView view) {
        this.mView = view;
    }

    @Override
    public void createView() {
        mView.initializeView();
    }

    @Override
    public void setUpToolbar() {
        mView.setUpToolbar();
    }

    @Override
    public void checkGpsState() {
        mView.checkGpsState();
    }

    @Override
    public void startCheckingForNewUpdates() {
        mView.startCheckingForNewUpdates();
    }

    @Override
    public void startServices() {
        mView.startServices();
    }

    @Override
    public void createBottomNav() {
        mView.initializeBottomNav();
    }

    @Override
    public void launchFragment(Fragment fragment) {
        mView.openFragment(fragment);
    }
}