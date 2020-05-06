package com.nearby.whatsnearby.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.RecyclerItemClickListener;
import com.nearby.whatsnearby.adapters.SearchResultAdapter;
import com.nearby.whatsnearby.beans.GooglePlacesBean;
import com.nearby.whatsnearby.beans.GooglePlacesParser;
import com.nearby.whatsnearby.beans.SearchItemBean;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.fragments.error.ErrorFragment;
import com.nearby.whatsnearby.interfaces.ISearchView;
import com.nearby.whatsnearby.presenters.SearchPresenter;
import com.nearby.whatsnearby.requests.NetworkTask;
import com.nearby.whatsnearby.services.GpsTracker;
import com.nearby.whatsnearby.utilities.AlertType;
import com.nearby.whatsnearby.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rudhraksh.pahade on 11-07-2016.
 */

public class ActivitySearch extends FragmentActivity {

    private AutoCompleteTextView mAutocompleteView;
    private FloatingActionButton getDirection = null;
    private ImageView back;
    private int cx;
    private int cy;
    private int startRadius = 0;
    private int endRadius = 0;
    private boolean hidden = true;
    private View myView;

    private List<SearchItemBean> results = new ArrayList<>();
    private SearchResultAdapter resultAdapter;
    private SearchItemBean search;

    private GpsTracker gpsTracker = null;
    private double latitude = 0;
    private double longitude = 0;

    private SweetAlertDialog sweetAlertDialog = null;

    private SearchPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mPresenter = new SearchPresenter(this, mSearchView);

        mPresenter.initView();

        checkGpsState();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mPresenter.hideSoftKeyboard();

        mPresenter.setSearchAdapter();
    }

    private void checkGpsState() {
        if (gpsTracker == null) {
            gpsTracker = new GpsTracker(getApplicationContext());
            if (gpsTracker.canGetLocation()) {
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();
            } else {
                showSettingsAlert();
            }
        }
    }

    private void showSettingsAlert() {
        if (sweetAlertDialog == null) {
            sweetAlertDialog = new SweetAlertDialog(ActivitySearch.this, SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setTitleText(getResources().getString(R.string.gps_disabled_alert_title));
            sweetAlertDialog.setContentText(getResources().getString(R.string.gps_disabled_alert_msg));
            sweetAlertDialog.setConfirmText((getResources().getString(R.string.gps_settings)));
            sweetAlertDialog.setCancelText(getResources().getString(R.string.gps_cancel));
            sweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });
            sweetAlertDialog.setCancelClickListener(SweetAlertDialog::dismissWithAnimation);
            sweetAlertDialog.show();
        }
    }

    private final FetchFromServerUser mServerResponse = new FetchFromServerUser() {
        @Override
        public void onPreFetch(AlertType alertType) {
            switch (alertType) {
                case AUTO_COMPLETE_SEARCH:
                    break;
            }
        }

        @Override
        public void onFetchCompletion(String string, int id, AlertType alertType) {
            switch (alertType) {
                case AUTO_COMPLETE_SEARCH:
                    if (string != null && !string.equals("")) {
                        Log.d("Result", string);
                        GooglePlacesParser parser = new GooglePlacesParser(string);
                        ArrayList<GooglePlacesBean> placesList = parser.getPlaces();
                        for (int i = 0; i < placesList.size(); i++) {
                            SearchItemBean bean = new SearchItemBean();
                            bean.setName(placesList.get(i).getDescription());
                            bean.setPlaceID(placesList.get(i).getPlaceId());
                            bean.setType("Google");
                            results.add(bean);
                        }
                        resultAdapter.notifyDataSetChanged();
                        RecyclerView resultList = findViewById(R.id.searchResult);
                        resultList.setLayoutManager(new LinearLayoutManager(ActivitySearch.this));
                        resultList.setAdapter(resultAdapter);
                        resultList.addOnItemTouchListener(new RecyclerItemClickListener(ActivitySearch.this, ((view, position) -> {
                            search = results.get(position);
                            mAutocompleteView.setText(search.getName());
                        })));
                    } else {
                        ErrorFragment errorFragment = new ErrorFragment();
                        Bundle msg = new Bundle();
                        msg.putString("msg", "No such place found.");
                        errorFragment.setArguments(msg);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.message, errorFragment).commit();
                    }
                    break;
            }
        }
    };

    private final ISearchView mSearchView = new ISearchView() {
        @Override
        public void initView() {
            // Setting navigation bar color
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
            myView = findViewById(R.id.searchBar);
            mAutocompleteView = findViewById(R.id.places_autocomplete);
            getDirection = findViewById(R.id.getDirection);
            myView.post(() -> mPresenter.startRevealAnimation());
            back = findViewById(R.id.back);
            mPresenter.initListeners();
        }

        @Override
        public void initListeners() {
            back.setOnClickListener(v -> ActivitySearch.this.finish());
            getDirection.setOnClickListener(v -> {
                if (search != null) {
                    mPresenter.navigateToPlaceDetails(search);
                }
                ActivitySearch.this.finish();
            });
            mAutocompleteView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    results.clear();
                    String url = Utils.getInstance().getSearchUrl(s.toString(), latitude, longitude);
                    Log.i("URL", url);
                    mPresenter.startPredictingSearch(url);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        @Override
        public void startRevealAnimation() {
            // finding X and Y co-ordinates
            cx = (myView.getLeft() + myView.getRight());
            cy = (myView.getTop());
            // to find  radius when icon is tapped for showing layout
            endRadius = Math.max(myView.getWidth(), myView.getHeight());
            // performing circular reveal when icon will be tapped
            Animator animator = ViewAnimationUtils.createCircularReveal(myView, cx, cy,
                    startRadius, endRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(400);

            //reverse animation
            // to find radius when icon is tapped again for hiding layout
            //  starting radius will be the radius or the extent to which circular reveal animation is to be shown

            int reverse_startradius = Math.max(myView.getWidth(), myView.getHeight());

            //endRadius will be zero
            int reverse_endradius = 0;

            // performing circular reveal for reverse animation
            Animator animate = ViewAnimationUtils.createCircularReveal(myView, cx, cy,
                    reverse_startradius, reverse_endradius);
            if (hidden) {
                // to show the layout when icon is tapped
                myView.setVisibility(View.VISIBLE);
                animator.start();
                hidden = false;
            } else {
                myView.setVisibility(View.VISIBLE);

                // to hide layout on animation end
                animate.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        myView.setVisibility(View.INVISIBLE);
                        hidden = true;
                    }
                });
                animate.start();
            }
        }

        @Override
        public void navigateToPlaceDetails(SearchItemBean searchItemBean) {
            Intent detailActivity = new Intent(ActivitySearch.this, PlaceDetail.class);
            detailActivity.putExtra("placeId", searchItemBean.getPlaceID());
            startActivity(detailActivity);
        }

        @Override
        public void startPredictingSearch(String url) {
            NetworkTask.getInstance(0)
                    .executeAutocompleteSearch(mServerResponse, url);
        }

        @Override
        public void setSearchAdapter() {
            resultAdapter = new SearchResultAdapter(ActivitySearch.this, results);
        }
    };
}