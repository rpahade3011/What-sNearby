package com.nearby.whatsnearby.presenters;

public interface IPlaceDetailsView {
    void notifyUIReady();
    void setupPlaceDetailsToolbar();
    void setupMap();
    void getUsersLocation();
    void setPlaceDetails();
    void setupGoogleMapStyle();
}