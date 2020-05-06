package com.nearby.whatsnearby.interfaces;

import androidx.fragment.app.Fragment;

public interface IBottomNavPresenter {
    void createView();
    void setUpToolbar();
    void checkGpsState();
    void startCheckingForNewUpdates();
    void startServices();
    void createBottomNav();
    void launchFragment(Fragment fragment);
    void navigateToPermissionScreen();
}