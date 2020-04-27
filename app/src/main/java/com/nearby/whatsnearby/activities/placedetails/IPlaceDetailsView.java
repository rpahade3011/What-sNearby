package com.nearby.whatsnearby.activities.placedetails;

public interface IPlaceDetailsView {
    void notifyUIReady();
    void setupPlaceDetailsToolbar();
    void setupMap();
    void getUsersLocation();
    void setPlaceDetails();
    void setUpBottomSheetContents();
}