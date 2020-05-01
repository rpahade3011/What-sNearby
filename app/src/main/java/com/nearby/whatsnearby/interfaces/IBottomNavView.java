package com.nearby.whatsnearby.interfaces;

import androidx.fragment.app.Fragment;

public interface IBottomNavView {
    void initializeView();
    void setUpToolbar();
    void checkGpsState();
    void startCheckingForNewUpdates();
    void startServices();
    void initializeBottomNav();
    void openFragment(Fragment fragment);
}