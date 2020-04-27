package com.nearby.whatsnearby.activities.placedetails;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nearby.whatsnearby.AlertType;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.activities.ActivityCall;
import com.nearby.whatsnearby.adapters.PlacesImageAdapterHolder;
import com.nearby.whatsnearby.adapters.ReviewRecyclerAdapter;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.bottomsheet.BottomSheetBehaviorGoogleMapsLike;
import com.nearby.whatsnearby.bottomsheet.MergedAppBarLayout;
import com.nearby.whatsnearby.bottomsheet.MergedAppBarLayoutBehavior;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.customasynctask.ExecuteDirectionsAPI;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.customasynctask.SharePlaceTask;
import com.nearby.whatsnearby.fragments.error.ErrorFragment;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.services.GpsTracker;
import com.nearby.whatsnearby.utilities.MapUtil;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.constants.IndicatorGravity;
import com.zhpan.bannerview.constants.TransformerStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ActivityPlaceDetails extends AppCompatActivity {

    private static final String LOG_TAG = "ActivityPlaceDetails";
    private static final int REQUEST_SHARE = 1010;
    private static final String NO_REVIEWS_TEXT = "Sorry, no reviews available for this place";

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
    private PlaceDetailBean.Review[] mDataReviewsArray;
    private String[] mDataPhotosArray;
    private boolean mDataIsOpen;
    private List<String> mListOfPlaceImages;

    private GoogleMap mMap;
    private View mGoogleMapLocationButtonView;
    private FloatingActionButton fabBtnNavigate;

    private SupportMapFragment mMapFragment;
    private GpsTracker mGpsTracker;
    private LatLng mUserSource;
    private LatLng mUserDestination;

    private BannerViewPager<String, PlacesImageAdapterHolder> mBannerViewPager;
    private CoordinatorLayout mMainCoordinatorLayout;

    // Circular Reveal variables
    private int cx, cy;
    private boolean hidden;
    private Animator animator;

    private BottomSheetBehaviorGoogleMapsLike mBottomSheetBehavior;
    private View mBottomSheet;
    private boolean shouldShowReviews = false;
    private boolean shouldShowPlaceImages = false;

    private Bitmap locationBitmap = null;
    private String imagePath = null;

    private int[] mPagerTransformArray = new int[] {
            TransformerStyle.NONE,
            TransformerStyle.ACCORDION,
            TransformerStyle.STACK,
            TransformerStyle.DEPTH,
            TransformerStyle.ROTATE,
            TransformerStyle.SCALE_IN
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
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
        fabBtnNavigate.setOnClickListener(mOnFabButtonClick);
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
            animator =
                    ViewAnimationUtils
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

    private void reArrangeGoogleMapLocationButton() {
        if (mGoogleMapLocationButtonView != null
                && mGoogleMapLocationButtonView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mGoogleMapLocationButtonView
                    .findViewById(Integer.parseInt("1")).getParent())
                    .findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }

    private void drawRoute() {
        String directionUrl = MapUtil.getInstance().getDirectionUrl(mUserSource, mUserDestination);
        Log.d("DirectionURL", directionUrl);
        new ExecuteDirectionsAPI(this, mMap)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, directionUrl);
        mPresenter.setPlaceDetails();
    }

    private void initializePlaceImages() {
        mBannerViewPager = findViewById(R.id.banner_view_pager);
        if (mDataPhotosArray != null && mDataPhotosArray.length > 0) {
            mListOfPlaceImages = convertStringArrayToList();
            mBannerViewPager.setVisibility(View.VISIBLE);
            mBannerViewPager.showIndicator(true)
                    .setPageTransformerStyle(mPagerTransformArray[new Random().nextInt(6)])
                    .setScrollDuration(1300)
                    .setInterval(3000)
                    .setCanLoop(true)
                    .setAutoPlay(true)
                    .setRoundCorner(getResources().getDimensionPixelOffset(R.dimen.dp_7))
                    .setIndicatorSliderColor(Color.parseColor("#935656"), Color.parseColor("#FF4C39"))
                    .setIndicatorGravity(IndicatorGravity.END)
                    .setScrollDuration(1000)
                    .setHolderCreator(PlacesImageAdapterHolder::new)
                    .create(mListOfPlaceImages);
        } else {
            mBannerViewPager.setVisibility(View.GONE);
        }
    }

    private void initializeReviews() {
        // Initializing Reviews
        View reviewView = findViewById(R.id.review_frame);
        if (!shouldShowReviews && reviewView != null) {
            reviewView.setVisibility(View.GONE);
        } else {
            reviewView.setVisibility(View.VISIBLE);
            RecyclerView reviews = reviewView.findViewById(R.id.review_list);
            reviews.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            reviews.setLayoutManager(layoutManager);

            PlaceDetailBean.Review[] reviewArray = mDataReviewsArray;

            if (reviewArray != null && reviewArray.length > 0) {
                ReviewRecyclerAdapter reviewsAdapter = new ReviewRecyclerAdapter(reviewArray,
                        getApplicationContext());
                reviews.setAdapter(reviewsAdapter);
            } else {
                TextView no_Review = reviewView.findViewById(R.id.no_reviews);
                no_Review.setText(NO_REVIEWS_TEXT);
            }
        }
    }

    private List<String> convertStringArrayToList() {
        List<String> listOfStrings = new ArrayList<>(Arrays.asList(mDataPhotosArray));
        return listOfStrings;
    }

    private final View.OnClickListener mOnFabButtonClick = v -> {
       startNavigation(mUserDestination);
    };

    private final OnMapReadyCallback mOnMapReadyCallback = googleMap -> {
        if (googleMap != null) {
            mMap = googleMap;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            //reArrangeGoogleMapLocationButton();
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setBuildingsEnabled(true);
            mMap.getFocusedBuilding();
            mMap.setTrafficEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.flag_marker))
                    .position(mUserDestination).title(mDataPlaceName));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mDataLat, mDataLng),
                    12.0f));
            drawRoute();
        }
    };

    private void openCallingDialog(String phoneNumber, String name) {
        Intent intent = new Intent(ActivityPlaceDetails.this,
                ActivityCall.class);
        intent.putExtra("personName", name);
        intent.putExtra("personContactNumber", phoneNumber.trim());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openSharePlace() {
        SharePlaceTask sharePlaceTask = new SharePlaceTask(mUserDestination.latitude,
                mUserDestination.longitude);
        locationBitmap = sharePlaceTask.execute();
        imagePath = MediaStore.Images.Media.insertImage(getContentResolver(),
                locationBitmap, mDataPlaceName, mDataPlaceAddress);
        assert imagePath != null;
        sharePlace();
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

    private void sharePlace() {
        if (imagePath != null) {
            Uri imageUri = Uri.parse(imagePath);
            if (imageUri != null) {
                Intent locationShareIntent = new Intent(Intent.ACTION_SEND);
                locationShareIntent.putExtra(Intent.EXTRA_TEXT, mDataPlaceName);
                locationShareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                locationShareIntent.setType("image/*");
                locationShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (locationShareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(locationShareIntent, "Share image via"),
                            REQUEST_SHARE);
                } else {
                    ErrorFragment errorFragment = new ErrorFragment();
                    Bundle msg = new Bundle();
                    msg.putString("msg", "Unable to share your location. No such application found to share.");
                    errorFragment.setArguments(msg);
                    getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment).commit();
                }
            } else {
                ErrorFragment errorFragment = new ErrorFragment();
                Bundle msg = new Bundle();
                msg.putString("msg", "Unable to share your location. No such application found to share.");
                errorFragment.setArguments(msg);
                getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment).commit();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Failed to share your location", Toast.LENGTH_SHORT).show();
        }
    }

    private final BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback mBottomSheetCallback
            = new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                    Log.d("bottomsheet-", "STATE_COLLAPSED");
                    shouldShowPlaceImages = false;
                    shouldShowReviews = false;
                    break;
                case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                    Log.d("bottomsheet-", "STATE_DRAGGING");
                    break;
                case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                    Log.d("bottomsheet-", "STATE_EXPANDED");
                    if (!shouldShowReviews) {
                        shouldShowReviews = true;
                        initializeReviews();
                    }
                    break;
                case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                    Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                    if (!shouldShowPlaceImages) {
                        shouldShowPlaceImages = true;
                        initializePlaceImages();
                    }
                    break;
                case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                    Log.d("bottomsheet-", "STATE_HIDDEN");
                    break;
                default:
                    Log.d("bottomsheet-", "STATE_SETTLING");
                    break;
                case BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING:
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

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
                actionBar.setTitle("Place details");
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
        }

        @Override
        public void setPlaceDetails() {
            mBottomSheet = mMainCoordinatorLayout.findViewById(R.id.bottom_sheet);
            mBottomSheetBehavior =
                    BottomSheetBehaviorGoogleMapsLike.from(mBottomSheet);
            mBottomSheetBehavior.addBottomSheetCallback(mBottomSheetCallback);

            //AppBarLayout mergedAppBarLayout = findViewById(R.id.merged_appbar_layout);
            MergedAppBarLayout mergedAppBarLayout = findViewById(R.id.merged_appbar_layout);
            MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior =
                    MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
            mergedAppBarLayoutBehavior.setToolbarTitle(mDataPlaceName);
            mergedAppBarLayoutBehavior.setNavigationOnClickListener(v ->
                    mBottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED));

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
                MapUtil.getInstance().getDistanceTimeETA().observe(ActivityPlaceDetails.this,
                        s -> {
                            if (s != null) {
                                placeETATv.setText(MapUtil.getInstance().getDistanceTimeETA().getValue());
                            } else {
                                String NO_ETA = "0 Mins";
                                placeETATv.setText(NO_ETA);
                            }
                        });
            }
            mBottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            mPresenter.setBottomDetails();
        }

        @Override
        public void setUpBottomSheetContents() {
            ImageButton buttonCall = mBottomSheet.findViewById(R.id.buttonCall);
            ImageButton buttonNavigate = mBottomSheet.findViewById(R.id.buttonNavigate);
            ImageButton buttonShare = mBottomSheet.findViewById(R.id.buttonShare);
            TextView placeAddressTv = mBottomSheet.findViewById(R.id.places_detail_address_detail);
            TextView placeCallTv = mBottomSheet.findViewById(R.id.places_detail_int_phone_detail);
            TextView placeCompoundTv = mBottomSheet.findViewById(R.id.places_detail_compound_detail);
            TextView placeTimingsTv = mBottomSheet.findViewById(R.id.places_detail_timings);

            placeAddressTv.setText(mDataPlaceAddress);
            placeCallTv.setText(mDataContactNumber);
            placeCompoundTv.setText(mDataCompoundAddress);
            if (mDataIsOpen) {
                placeTimingsTv.setTextColor(Color.parseColor("#2E7D32"));
                placeTimingsTv.setText("Currently Open");
            } else {
                placeTimingsTv.setTextColor(Color.parseColor("#D50000"));
                placeTimingsTv.setText("Currently Closed");
            }

            // Setting calling enabled to phone numbers
            buttonCall.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(placeCallTv.getText().toString())) {
                    openCallingDialog(placeCallTv.getText().toString(), mDataPlaceName);
                }
            });

            // Setting navigation to particular destination
            buttonNavigate.setOnClickListener(v -> startNavigation(mUserDestination));

            buttonShare.setOnClickListener(v -> openSharePlace());
        }
    };
}