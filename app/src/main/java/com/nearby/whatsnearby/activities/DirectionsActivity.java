package com.nearby.whatsnearby.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.customasynctask.ExecuteDirectionsAPI;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.presenters.IPlaceDetailsView;
import com.nearby.whatsnearby.presenters.PlaceDetailsPresenter;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.services.GpsTracker;
import com.nearby.whatsnearby.utilities.AlertType;
import com.nearby.whatsnearby.utilities.MapUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DirectionsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "DirectionsActivity";

    // Presenter
    private PlaceDetailsPresenter mPresenter;

    // Intent data
    private double mDataLat;
    private double mDataLng;
    private String mDataPlaceName;
    private String mDataCompoundAddress;
    private float mDataPlaceRatings;
    private String mDataContactNumber;
    private String mDataPlaceCategory;
    private String mDataPlaceAddress;
    private String mDataWebsiteUrl;
    private PlaceDetailBean.Review[] mDataReviewsArray;
    private String[] mDataPhotosArray;
    private boolean mDataIsOpen;

    private GoogleMap mMap;
    private View mGoogleMapLocationButtonView;
    private LinearLayout fabBtnNavigate;
    private LinearLayout tapLayout;

    private SupportMapFragment mMapFragment;
    private GpsTracker mGpsTracker;
    private LatLng mUserSource;
    private LatLng mUserDestination;

    private CoordinatorLayout mMainCoordinatorLayout;

    // Circular Reveal variables
    private int cx, cy;
    private boolean hidden;
    private Animator animator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btm_sheet_full);
        mPresenter = new PlaceDetailsPresenter(mPlaceDetailsView);
        mPresenter.initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(mOnMapReadyCallback);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createExitReveal();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Retrieves data.
     */
    private void getIntentData() {
        Intent data = getIntent();
        if (data != null) {
            Bundle bundleData = data.getExtras();
            if (bundleData != null) {
                try {
                    mDataLat = bundleData.getDouble("Lat");
                    mDataLng = bundleData.getDouble("Lng");
                    mDataPlaceName = bundleData.getString("Name");
                    mDataPlaceAddress = bundleData.getString("Address");
                    mDataIsOpen = bundleData.getBoolean("Timing");
                    mDataPlaceCategory = bundleData.getString("Place_Category");
                    mDataCompoundAddress = bundleData.getString("CompoundAddress");
                    mDataContactNumber = bundleData.getString("ContactNumber");
                    mDataWebsiteUrl = bundleData.getString("Place_Website");
                    mDataPlaceRatings = bundleData.getFloat("PlaceRatings");
                    mDataPhotosArray = AppController.getInstance().getPlacePhotos();
                    mDataReviewsArray = AppController.getInstance().getReview();
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "Exception while retrieving intent data --> "
                            + ex.getLocalizedMessage());
                }
            }
        }
    }

    private void initializeUI() {
        mMainCoordinatorLayout = findViewById(R.id.coordinator_layout);
        mMainCoordinatorLayout.post((this::createImageReveal));
        fabBtnNavigate = findViewById(R.id.fabNavigate);
        tapLayout = findViewById(R.id.tap_layout);
        fabBtnNavigate.setOnClickListener(mOnFabButtonClick);
        tapLayout.setOnClickListener(mDownArrowClickListener);
    }

    private void createImageReveal() {
        View profileImageView = mMainCoordinatorLayout;

        // get the center for the clipping circle
        cx = (profileImageView.getLeft() + profileImageView.getRight()) / 2;
        cy = (profileImageView.getTop() + profileImageView.getBottom()) / 2;

        // get the final radius for the clipping circle
        int dx = Math.max(cx, profileImageView.getWidth() - cx);
        int dy = Math.max(cy, profileImageView.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        // Android native animator
        try {
            animator = ViewAnimationUtils
                    .createCircularReveal(profileImageView, cx, cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(500);
            animator.start();
        } catch (Exception e) {
            e.printStackTrace();
            animator = null;
        }

        hidden = false;
    }

    private void createExitReveal() {
        View profileImageView = mMainCoordinatorLayout;

        cx = (profileImageView.getLeft() + profileImageView.getRight()) / 2;
        cy = (profileImageView.getTop() + profileImageView.getBottom()) / 2;

        // get the final radius for the clipping circle
        int dx = Math.max(cx, profileImageView.getWidth() - cx);
        int dy = Math.max(cy, profileImageView.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        //end radius will be zero
        int reverse_endradius = 0;

        // performing circular reveal for reverse animation
        Animator animate = ViewAnimationUtils
                .createCircularReveal(profileImageView, cx, cy, finalRadius, reverse_endradius);
        if (hidden) {
            // to show the layout when icon is tapped
            mMainCoordinatorLayout.setVisibility(View.VISIBLE);
            animator.setDuration(1500);
            animator.start();
            hidden = false;
        } else {
            // to hide layout on animation end
            animate.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    hidden = true;
                }
            });
            animate.start();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    private void drawRoute() {
        String directionUrl = MapUtil.getInstance().getDirectionUrl(mUserSource, mUserDestination);
        Log.d("DirectionURL", directionUrl);
        new ExecuteDirectionsAPI(this, mMap)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, directionUrl);
    }

    private void startNavigation(LatLng dest) {
        //The intent chooser displays all the installed apps.
        String suffixCoordinatesLink = dest.latitude + "," + dest.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //build the link url
        StringBuilder link = new StringBuilder();
        link.append(GlobalSettings.PREFIX_DEFAULT_GOOGLE_NAVIGATE + suffixCoordinatesLink);
        intent.setData(Uri.parse(link.toString()));

        if (intent.resolveActivity(this.getPackageManager()) != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                anfe.printStackTrace();
            }
        } else {
            Toast.makeText(this.getApplicationContext(),
                    "Could not start the activity", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent getPlaceOverviewDetailsIntent() {
        Intent intent = new Intent(DirectionsActivity.this, DetailsOverviewActivity.class);
        Bundle data = new Bundle();
        data.putDouble("Lat", mDataLat);
        data.putDouble("Lng", mDataLng);
        data.putString("Name", mDataPlaceName);
        data.putString("Address", mDataPlaceAddress);
        data.putBoolean("Timing", mDataIsOpen);
        data.putString("Place_Category", mDataPlaceCategory);
        data.putString("CompoundAddress", mDataCompoundAddress);
        data.putString("ContactNumber", mDataContactNumber);
        data.putFloat("PlaceRatings", mDataPlaceRatings);
        data.putString("Place_Website", mDataWebsiteUrl);
        intent.putExtras(data);
        return intent;
    }

    private void reArrangeLocationButton() {
        View locationButton = ((View) mGoogleMapLocationButtonView.findViewById(Integer.parseInt("1"))
                .getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
    }

    private final View.OnClickListener mOnFabButtonClick = v -> {
        startNavigation(mUserDestination);
    };

    private final View.OnClickListener mDownArrowClickListener = v -> {
        Intent intent = getPlaceOverviewDetailsIntent();
        String transitionName = getResources().getString(R.string.transition_maps_detail);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(DirectionsActivity.this,
                        v, transitionName);
        ActivityCompat.startActivity(DirectionsActivity.this, intent, options.toBundle());
    };

    private final OnMapReadyCallback mOnMapReadyCallback = googleMap -> {
        if (googleMap != null) {
            mMap = googleMap;
            mPresenter.setupMapStyle();
            mMap.setMaxZoomPreference(20);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            reArrangeLocationButton();
            mMap.setBuildingsEnabled(true);
            mMap.getFocusedBuilding();
            mMap.setTrafficEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mDataLat, mDataLng))
                    .zoom(10)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            drawRoute();
        }
    };

    private final FetchFromServerUser mServerResponse = new FetchFromServerUser() {
        @Override
        public void onPreFetch(AlertType alertType) {
            switch (alertType) {
                case CALCULATE_DISTANCE_BETWEEN_TWO_LOCATIONS:
                    break;
            }
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onFetchCompletion(String string, int id, AlertType alertType) {
            switch (alertType) {
                case CALCULATE_DISTANCE_BETWEEN_TWO_LOCATIONS:
                    if (string != null) {
                        Log.i(LOG_TAG, "execute() - " + string);
                        try {
                            JSONObject resultJson = new JSONObject(string);

                            JSONArray rowArray = resultJson.getJSONArray("rows");
                            JSONObject jsonObject = rowArray.getJSONObject(0);

                            JSONArray elementArray = jsonObject.getJSONArray("elements");

                            JSONObject finalObject = elementArray.getJSONObject(0);

                            JSONObject durationObject = finalObject.getJSONObject("duration");

                            String durationText = durationObject.optString("text");
                            int durationValue = durationObject.getInt("value");

                            MapUtil.getInstance().setDistanceAndTimeETA(durationText);

                            JSONObject distanceObject = finalObject.getJSONObject("distance");

                            String dText = distanceObject.optString("text");
                            int distanceValue = distanceObject.getInt("value");
                            double roundOff = Math.round((distanceValue / 1609.34) * 100.0) / 100.0;

                            String distanceText = dText.replace(",", ".");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    private final IPlaceDetailsView mPlaceDetailsView = new IPlaceDetailsView() {
        @Override
        public void notifyUIReady() {
            getIntentData();
            initializeUI();
            mPresenter.setupToolbar();
            mPresenter.setUpMapIfNeeded();
        }

        @Override
        public void setupPlaceDetailsToolbar() {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mDataPlaceName);
                // Setting navigation bar color for lollipop devices
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }

        @Override
        public void setupMap() {
            mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            assert mMapFragment != null;
            mGoogleMapLocationButtonView = mMapFragment.getView();
            mPresenter.getCurrentLocation();
        }

        @Override
        public void getUsersLocation() {
            mGpsTracker = new GpsTracker(getApplicationContext());
            mUserSource = new LatLng(mGpsTracker.getLatitude(), mGpsTracker.getLongitude());
            mUserDestination = new LatLng(mDataLat, mDataLng);
            MapUtil.getInstance().setSourceBounds(mUserSource);
            MapUtil.getInstance().setDestinationBounds(mUserDestination);
            mPresenter.setPlaceDetails();
        }

        @Override
        public void setPlaceDetails() {
            TextView bottomSheetTitle = findViewById(R.id.bottom_sheet_title);
            RatingBar placeRatings = findViewById(R.id.bottom_sheet_rating);
            TextView placeRatingsTotalTv = findViewById(R.id.bottom_sheet_total_ratings);
            TextView placeCategoryTv = findViewById(R.id.bottom_sheet_category);
            TextView placeETATv = findViewById(R.id.bottom_sheet_eta);

            bottomSheetTitle.setText(mDataPlaceName);

            if (mDataReviewsArray != null && mDataReviewsArray.length > 0) {
                placeRatings.setRating(mDataPlaceRatings);
                placeRatingsTotalTv.setText(getApplicationContext().getResources()
                        .getString(R.string.number_of_reviews, mDataReviewsArray.length));
            } else {
                placeRatings.setRating(0);
                placeRatingsTotalTv.setText(getApplicationContext().getResources()
                        .getString(R.string.number_of_reviews, 0));
            }

            placeCategoryTv.setText(mDataPlaceCategory);

            // Setting Distance and Time ETA
            if (mUserSource != null && mUserDestination != null) {
                MapUtil.getInstance().calculateNearbyDistance(mServerResponse,
                        mUserSource, mUserDestination);
                MapUtil.getInstance().getDistanceTimeETA().observe(DirectionsActivity.this,
                        s -> {
                            if (s != null) {
                                placeETATv.setText(MapUtil.getInstance().getDistanceTimeETA().getValue());
                            } else {
                                String NO_ETA = "0 Mins";
                                placeETATv.setText(NO_ETA);
                            }
                        });
            }
        }

        @Override
        public void setupGoogleMapStyle() {
            if (mMap != null) {
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = mMap.setMapStyle(MapStyleOptions
                            .loadRawResourceStyle(getApplicationContext(), R.raw.map_style_silver));
                    if (!success) {
                        Log.e(LOG_TAG, "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e(LOG_TAG, "Can't find style. Error: ", e);
                }
            }
        }
    };
}