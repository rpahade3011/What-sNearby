package com.nearby.whatsnearby.presenters;

public interface IPlaceDetailsPresenter {
    void initializeUI();
    void setupToolbar();
    void setUpMapIfNeeded();
    void getCurrentLocation();
    void setPlaceDetails();
    void setupMapStyle();
}