package com.nearby.whatsnearby.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nearby.whatsnearby.AlertType;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.PlacesImageAdapter;
import com.nearby.whatsnearby.adapters.ReviewRecyclerAdapter;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.bottomsheet.BottomSheetBehaviorGoogleMapsLike;
import com.nearby.whatsnearby.bottomsheet.MergedAppBarLayoutBehavior;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.customasynctask.ExecuteDirectionsAPI;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.customasynctask.SharePlaceTask;
import com.nearby.whatsnearby.fragments.error.ErrorFragment;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.services.GpsTracker;
import com.nearby.whatsnearby.utilities.MapUtil;
import com.nearby.whatsnearby.views.PagerAnimation;
import com.wizchen.topmessage.TopMessage;
import com.wizchen.topmessage.TopMessageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rudhraksh.pahade on 8/3/2016.
 */

public class AboutPlaceDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AboutPlaceDetailActivity";
    private static final int REQUEST_SHARE = 1010;


    private static final String NO_REVIEWS_TEXT = "Sorry, no reviews available for this place";

    private TextView bottomSheetTextView;
    private TextView txtReviewsSize;
    private TextView tvDistanceETA;
    private double lat;
    private double lng;
    private String placeName;
    private float placeRatings;
    private String contactNumber;
    private String placeAddress;
    private PlaceDetailBean.Review[] reviewsArray;
    private String[] photosArray;
    private GoogleMap mMap;
    private FragmentManager fm;
    private SupportMapFragment fragment;
    private GpsTracker gpsTracker;
    private LatLng source;
    private LatLng destination;
    private Bitmap locationBitmap = null;

    private String imagePath = null;
    private boolean shouldShowReviews = false;
    private boolean shouldShowPlaceImages = false;

    private FloatingActionButton fabNavigate;
    private ViewPager photosViewPager = null;
    private CoordinatorLayout coordinatorLayout = null;

    // Circular Reveal variables
    int cx, cy;
    boolean hidden;
    Animator animator;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_about_fragment);
        coordinatorLayout = findViewById(R.id.coordinatorlayout);

        getIntentData();

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            coordinatorLayout.post((this::createImageReveal));
        }
        setUpMapIfNeeded();
        getUsersLocation();
        fabNavigate = findViewById(R.id.fabNavigate);
        photosViewPager = findViewById (R.id.pager);
        tvDistanceETA = findViewById (R.id.tvDistanceETA);

        /**
         * If we want to listen for states callback
         */
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehaviorGoogleMapsLike behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet,
                                       @BottomSheetBehaviorGoogleMapsLike.State int newState) {
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
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        AppBarLayout mergedAppBarLayout = findViewById(R.id.merged_appbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior =
                MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle(placeName);
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(v ->
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED));

        bottomSheetTextView = bottomSheet.findViewById(R.id.bottom_sheet_title);
        txtReviewsSize = bottomSheet.findViewById(R.id.text_dummy1);

        bottomSheetTextView.setText(placeName);
        if (reviewsArray != null && reviewsArray.length > 0) {
            txtReviewsSize.setText("" + reviewsArray.length + " reviews");
        } else {
            txtReviewsSize.setText("No reviews");
        }

        // Setting Distance and Time ETA
        if (source != null && destination != null) {
            MapUtil.getInstance().calculateNearbyDistance(mServerResponse, source, destination);
            if (MapUtil.getInstance().getDistanceAndTimeETA() != null) {
                tvDistanceETA.setText(MapUtil.getInstance().getDistanceAndTimeETA());
            } else {
                String NO_ETA = "0 Mins";
                tvDistanceETA.setText(NO_ETA);
            }
        }

        try {
            setUpBottomSheetContents(bottomSheet);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Exception");
        }

        fabNavigate.setOnClickListener(v -> startNavigation(destination));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fragment != null) {
            fragment.getMapAsync(mOnMapReadyCallbacks);
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

    private void getIntentData() {
        Intent data = getIntent();
        if (data != null) {
            Bundle bundleData = data.getExtras();
            if (bundleData != null) {
                try {
                    lat = bundleData.getDouble("Lat");
                    lng = bundleData.getDouble("Lng");
                    placeName = bundleData.getString("Name");
                    placeAddress = bundleData.getString("Address");
                    contactNumber = bundleData.getString("ContactNumber");
                    placeRatings = bundleData.getFloat("PlaceRatings");
                    photosArray = AppController.getInstance().getPlacePhotos();
                    reviewsArray = AppController.getInstance().getReview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createImageReveal() {

        View profileImageView = coordinatorLayout;

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

        View profileImageView = coordinatorLayout;

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
            coordinatorLayout.setVisibility(View.VISIBLE);
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

    private void setUpMapIfNeeded() {
        fm = getSupportFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment != null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
    }

    private void getUsersLocation() {
        gpsTracker = new GpsTracker(getApplicationContext());
        source = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        destination = new LatLng(lat, lng);
        MapUtil.getInstance().setSourceBounds(source);
        MapUtil.getInstance().setDestinationBounds(destination);
    }

    private void setUpBottomSheetContents(View bottomSheet) throws Exception {
        ImageButton buttonCall = bottomSheet.findViewById(R.id.buttonCall);
        ImageButton buttonNavigate = bottomSheet.findViewById(R.id.buttonNavigate);
        ImageButton buttonShare = bottomSheet.findViewById(R.id.buttonShare);

        final TextView places_detail_int_phone_detail = bottomSheet.findViewById(R.id.places_detail_int_phone_detail);
        TextView places_detail_address_detail = bottomSheet.findViewById(R.id.places_detail_address_detail);
        RatingBar places_rating = bottomSheet.findViewById(R.id.rating);

        places_detail_int_phone_detail.setText(contactNumber);
        places_detail_address_detail.setText(placeAddress);

        Drawable ratingStars = places_rating.getProgressDrawable();
        DrawableCompat.setTint(ratingStars, Color.parseColor("#30d1d5"));
        places_rating.setRating(placeRatings);

        // Setting calling enabled to phone numbers
        buttonCall.setOnClickListener(v ->
                openCallingDialog(places_detail_int_phone_detail.getText().toString(), placeName));

        // Setting navigation to particular destination
        buttonNavigate.setOnClickListener(v -> startNavigation(destination));

        buttonShare.setOnClickListener(v -> openSharePlace());
    }

    private void initializePlaceImages() {
        PlacesImageAdapter placesImageAdapter = new PlacesImageAdapter(getApplicationContext(), photosArray);
        photosViewPager.setAdapter(placesImageAdapter);
        photosViewPager.setPageTransformer(true, new PagerAnimation());
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
            PlaceDetailBean.Review[] reviewArray = reviewsArray;
            if (reviewArray != null && reviewsArray.length > 0) {
                ReviewRecyclerAdapter reviewsAdapter = new ReviewRecyclerAdapter(reviewArray,
                        getApplicationContext());
                reviews.setAdapter(reviewsAdapter);
            } else {
                TextView no_Review = reviewView.findViewById(R.id.no_reviews);
                no_Review.setText(NO_REVIEWS_TEXT);
            }
        }
    }

    private void openCallingDialog(String phoneNumber, String name) {
        Intent intent = new Intent(AboutPlaceDetailActivity.this, ActivityCall.class);
        intent.putExtra("personName", name);
        intent.putExtra("personContactNumber", phoneNumber.trim());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @SuppressLint("LongLogTag")
    private void openSharePlace() {

        SharePlaceTask sharePlaceTask = new SharePlaceTask(destination.latitude, destination.longitude);
        locationBitmap = sharePlaceTask.execute();
        imagePath = MediaStore.Images.Media.insertImage(getContentResolver(),
                locationBitmap, placeName, placeAddress);
        assert imagePath != null;
        sharePlace();
    }

    private void sharePlace() {
        if (imagePath != null) {
            Uri imageUri = Uri.parse(imagePath);
            if (imageUri != null) {
                Intent locationShareIntent = new Intent(Intent.ACTION_SEND);
                locationShareIntent.putExtra(Intent.EXTRA_TEXT, placeName);
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

    private void drawRoute() {
        String directionUrl = MapUtil.getInstance().getDirectionUrl(source, destination);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SHARE:
                if (resultCode == Activity.RESULT_CANCELED) {
                    TopMessageManager.showSuccess("You've successfully shared "
                            + placeName, "", TopMessage.DURATION.LONG);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createExitReveal();
        } else {
            super.onBackPressed();
        }
    }

    private final OnMapReadyCallback mOnMapReadyCallbacks = googleMap -> {
        if (googleMap != null) {
            mMap = googleMap;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setBuildingsEnabled(true);
            mMap.getFocusedBuilding();
            mMap.setTrafficEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.flag_marker))
                    .position(destination).title(placeName));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14.0f));
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
}