package com.nearby.whatsnearby.interfaces;

import com.nearby.whatsnearby.beans.SearchItemBean;

public interface ISearchPresenter {
    void initView();
    void initListeners();
    void startRevealAnimation();
    void navigateToPlaceDetails(SearchItemBean searchItemBean);
    void startPredictingSearch(String url);
    void setSearchAdapter();
}