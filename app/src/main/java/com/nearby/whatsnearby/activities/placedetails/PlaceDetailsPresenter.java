package com.nearby.whatsnearby.activities.placedetails;

public class PlaceDetailsPresenter implements IPlaceDetailsPresenter {

    private IPlaceDetailsView mView;

    public PlaceDetailsPresenter(IPlaceDetailsView view) {
        this.mView = view;
    }

    @Override
    public void initializeUI() {
        if (mView != null) {
            mView.notifyUIReady();
        }
    }

    @Override
    public void setupToolbar() {
        if (mView != null) {
            mView.setupPlaceDetailsToolbar();
        }
    }

    @Override
    public void setUpMapIfNeeded() {
        if (mView != null) {
            mView.setupMap();
        }
    }

    @Override
    public void getCurrentLocation() {
        if (mView != null) {
            mView.getUsersLocation();
        }
    }

    @Override
    public void setPlaceDetails() {
        if (mView != null) {
            mView.setPlaceDetails();
        }
    }

    @Override
    public void setBottomDetails() {
        if (mView != null) {
            mView.setUpBottomSheetContents();
        }
    }
}