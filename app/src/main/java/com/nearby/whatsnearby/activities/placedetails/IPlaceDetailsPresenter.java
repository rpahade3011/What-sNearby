package com.nearby.whatsnearby.activities.placedetails;

public interface IPlaceDetailsPresenter {
    void initializeUI();
    void setupToolbar();
    void setUpMapIfNeeded();
    void getCurrentLocation();
    void setPlaceDetails();
    void setBottomDetails();
}