package com.nearby.whatsnearby.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.SearchResultAdapter;
import com.nearby.whatsnearby.beans.GooglePlacesBean;
import com.nearby.whatsnearby.beans.GooglePlacesParser;
import com.nearby.whatsnearby.beans.SearchItemBean;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.customasynctask.FetchFromServerTask;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.fragments.ErrorFragment;
import com.nearby.whatsnearby.services.GpsTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rudhraksh.pahade on 11-07-2016.
 */

public class ActivitySearch extends FragmentActivity implements FetchFromServerUser {

    public static final String GOOGLE_PLACES_URL = "maps.googleapis.com/maps/api/place/autocomplete/json";
    public static final int SEARCH_RADIUS = 1000;
    public static final String PLACES_API_KEY = "AIzaSyA2nMz4vfd-wyeivmvJffVB5RP59POoTm0";

    private AutoCompleteTextView mAutocompleteView;
    private FloatingActionButton getDirection = null;
    private int cx;
    private int cy;
    private int startRadius = 0;
    private int endRadius = 0;
    private boolean hidden = true;
    View myView;

    List<SearchItemBean> results = new ArrayList<>();
    SearchResultAdapter resultAdapter;
    SearchItemBean search;

    private GpsTracker gpsTracker = null;
    private double latitude = 0;
    private double longitude = 0;

    private SweetAlertDialog sweetAlertDialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        myView = findViewById(R.id.searchBar);
        myView.post(new Runnable() {
            @Override
            public void run() {
                createRevealLayout();
            }
        });
        checkGpsState();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        hideSoftKeyboard(ActivitySearch.this);

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitySearch.this.finish();
            }
        });


        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.places_autocomplete);
        getDirection = (FloatingActionButton) findViewById(R.id.getDirection);
        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search != null) {
                    Intent detailActivity = new Intent(ActivitySearch.this, PlaceDetail.class);
                    detailActivity.putExtra("placeId", search.getPlaceID());
                    startActivity(detailActivity);
                }
            }
        });
        resultAdapter = new SearchResultAdapter(this, results);
        mAutocompleteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                results.clear();
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .encodedAuthority(GOOGLE_PLACES_URL)
                        .appendQueryParameter("input", s.toString())
                        .appendQueryParameter("location", latitude + "," + longitude)
                        .appendQueryParameter("radius", String.valueOf(SEARCH_RADIUS))
                        .appendQueryParameter("key", PLACES_API_KEY);

                String url = builder.build().toString();
                Log.e("URL", url);
                new FetchFromServerTask(ActivitySearch.this, 0).execute(url);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * <p>When this activity is called we have some EditTexts whose behavior is to get input from
     * device keyboard and pops up the keyboard on screens. This method will help us to hide
     * the soft input of keyboard when this activity comes forward.</p>
     *
     * @param activity Activity: an activity is required as parameter to perform application-specific
     *                 tasks.
     */
    private void hideSoftKeyboard(Activity activity) {
        View v = activity.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createRevealLayout() {
        // finding X and Y co-ordinates
        cx = (myView.getLeft() + myView.getRight());
        cy = (myView.getTop());
        // to find  radius when icon is tapped for showing layout
        endRadius = Math.max(myView.getWidth(), myView.getHeight());
        // performing circular reveal when icon will be tapped
        Animator animator = ViewAnimationUtils.createCircularReveal(myView, cx, cy, startRadius, endRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(400);

        //reverse animation
        // to find radius when icon is tapped again for hiding layout
        //  starting radius will be the radius or the extent to which circular reveal animation is to be shown

        int reverse_startradius = Math.max(myView.getWidth(), myView.getHeight());

        //endRadius will be zero
        int reverse_endradius = 0;

        // performing circular reveal for reverse animation
        Animator animate = ViewAnimationUtils.createCircularReveal(myView, cx, cy, reverse_startradius, reverse_endradius);
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
            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
            sweetAlertDialog.show();
        }

    }


    @Override
    public void onPreFetch() {

    }

    @Override
    public void onFetchCompletion(String string, int id) {
        if (string != null && !string.equals("")) {
            Log.e("Result", string);
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
            ListView resultList = (ListView) findViewById(R.id.searchResult);
            resultList.setAdapter(resultAdapter);
            resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    search = results.get(position);
                    mAutocompleteView.setText(search.getName());
                }
            });
        } else {
            ErrorFragment errorFragment = new ErrorFragment();
            Bundle msg = new Bundle();
            msg.putString("msg", "No such place found.");
            errorFragment.setArguments(msg);
            getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment).commit();
        }
    }
}
