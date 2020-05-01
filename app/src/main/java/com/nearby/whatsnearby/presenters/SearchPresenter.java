package com.nearby.whatsnearby.presenters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.nearby.whatsnearby.beans.SearchItemBean;
import com.nearby.whatsnearby.interfaces.ISearchPresenter;
import com.nearby.whatsnearby.interfaces.ISearchView;

import java.lang.ref.WeakReference;

public class SearchPresenter implements ISearchPresenter {

    private WeakReference<Activity> mActivity;
    private ISearchView mView;

    public SearchPresenter(Activity activity, ISearchView view) {
        this.mActivity = new WeakReference<>(activity);
        this.mView = view;
    }

    /**
     * <p>When this activity is called we have some EditTexts whose behavior is to get input from
     * device keyboard and pops up the keyboard on screens. This method will help us to hide
     * the soft input of keyboard when this activity comes forward.</p>.
     */
    public void hideSoftKeyboard() {
        View v = mActivity.get().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.get()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public void initView() {
        mView.initView();
    }

    @Override
    public void initListeners() {
        mView.initListeners();
    }

    @Override
    public void startRevealAnimation() {
        mView.startRevealAnimation();
    }

    @Override
    public void navigateToPlaceDetails(SearchItemBean searchItemBean) {
        mView.navigateToPlaceDetails(searchItemBean);
    }

    @Override
    public void startPredictingSearch(String url) {
        mView.startPredictingSearch(url);
    }

    @Override
    public void setSearchAdapter() {
        mView.setSearchAdapter();
    }
}