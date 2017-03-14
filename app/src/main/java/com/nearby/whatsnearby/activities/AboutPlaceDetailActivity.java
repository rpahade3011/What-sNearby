package com.nearby.whatsnearby.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.PlacesImageAdapter;
import com.nearby.whatsnearby.adapters.ReviewAdapter;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.bottomsheet.BottomSheetBehaviorGoogleMapsLike;
import com.nearby.whatsnearby.bottomsheet.MergedAppBarLayoutBehavior;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.customasynctask.ExecuteDirectionsAPI;
import com.nearby.whatsnearby.fragments.ErrorFragment;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.services.GpsTracker;
import com.nearby.whatsnearby.utilities.MapUtil;
import com.nearby.whatsnearby.views.PagerAnimation;
import com.wizchen.topmessage.TopMessage;
import com.wizchen.topmessage.TopMessageManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by rudhraksh.pahade on 8/3/2016.
 */

public class AboutPlaceDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AboutPlaceDetailActivity";
    private static final int REQUEST_SHARE = 1010;
    private static final String PREFIX_DEFAULT_GOOGLE_NAVIGATE = "google.navigation:q=";

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

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_about_fragment);

        getIntentData();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Place details");
            // Setting navigation bar color for lollipop devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }

        setUpMapIfNeeded();
        getUsersLocation();
        fabNavigate = (FloatingActionButton) findViewById(R.id.fabNavigate);
        photosViewPager = (ViewPager) findViewById (R.id.pager);
        tvDistanceETA = (TextView) findViewById (R.id.tvDistanceETA);
        /**
         * If we want to listen for states callback
         */
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehaviorGoogleMapsLike behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehaviorGoogleMapsLike.State int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        shouldShowPlaceImages = false;
                        shouldShowReviews = false;
                        initializePlaceImages();
                        initializeReviews();
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");

                        if (!shouldShowPlaceImages) {
                            shouldShowPlaceImages = true;
                            initializePlaceImages();
                        }

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

        AppBarLayout mergedAppBarLayout = (AppBarLayout) findViewById(R.id.merged_appbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle(placeName);
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });

        bottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);
        txtReviewsSize = (TextView) bottomSheet.findViewById(R.id.text_dummy1);

        bottomSheetTextView.setText(placeName);
        if (reviewsArray != null && reviewsArray.length > 0) {
            txtReviewsSize.setText("" + reviewsArray.length + " reviews");
        } else {
            txtReviewsSize.setText("No reviews");
        }

        // Setting Distance and Time ETA
        if (source != null && destination != null) {
            MapUtil.calculateNearbyDistance(source, destination);
            if (GlobalSettings.DISTANCE_AND_TIME_ETA != null) {
                tvDistanceETA.setText(GlobalSettings.DISTANCE_AND_TIME_ETA);
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

        fabNavigate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation(destination);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap == null) {
            mMap = fragment.getMap();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ub_pin_pickup))
                    .position(source).title("My location"));
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ub_pin_destination))
                    .position(destination).title(placeName));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12.0f));
            drawRoute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(AboutPlaceDetailActivity.this, PlacesMain.class));
                AboutPlaceDetailActivity.this.finish();
                break;
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

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            fm = getSupportFragmentManager();
            fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
            if (fragment == null) {
                fragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.map, fragment).commit();
            }
        }
    }

    private void getUsersLocation() {
        gpsTracker = new GpsTracker(getApplicationContext());
        source = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        destination = new LatLng(lat, lng);
        MapUtil.sourceBounds = source;
        MapUtil.destinationBounds = destination;
    }

    private void setUpBottomSheetContents(View bottomSheet) throws Exception {
        ImageButton buttonCall = (ImageButton) bottomSheet.findViewById(R.id.buttonCall);
        ImageButton buttonNavigate = (ImageButton) bottomSheet.findViewById(R.id.buttonNavigate);
        ImageButton buttonShare = (ImageButton) bottomSheet.findViewById(R.id.buttonShare);

        final TextView places_detail_int_phone_detail = (TextView) bottomSheet.findViewById(R.id.places_detail_int_phone_detail);
        TextView places_detail_address_detail = (TextView) bottomSheet.findViewById(R.id.places_detail_address_detail);
        RatingBar places_rating = (RatingBar) bottomSheet.findViewById(R.id.rating);

        places_detail_int_phone_detail.setText(contactNumber);
        places_detail_address_detail.setText(placeAddress);

        Drawable ratingStars = places_rating.getProgressDrawable();
        DrawableCompat.setTint(ratingStars, Color.parseColor("#30d1d5"));
        places_rating.setRating(placeRatings);

        // Setting calling enabled to phone numbers
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCallingDialog(places_detail_int_phone_detail.getText().toString(), placeName);
            }
        });

        // Setting navigation to particular destination
        buttonNavigate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation(destination);
            }
        });

        buttonShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openSharePlace();
            }
        });
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
            ListView reviews = (ListView) reviewView.findViewById(R.id.review_list);
            PlaceDetailBean.Review[] reviewArray = reviewsArray;
            if (reviewArray != null && reviewsArray.length > 0) {
                ReviewAdapter reviewsAdapter = new ReviewAdapter(reviewArray, getApplicationContext());
                reviews.setAdapter(reviewsAdapter);
            } else {
                TextView no_Review = (TextView) reviewView.findViewById(R.id.no_reviews);
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

        final String locationUrl = getResources().getString(R.string.google_map_static_api_url)
                + destination.latitude + "," + destination.longitude
                + "&zoom=13&size=100x100&scale=2&format=jpeg&maptype=roadmap"
                + "&markers=color:blue" + "|" + "label:" + "|" + destination.latitude
                + "," + destination.longitude + "&key="
                + getResources().getString(R.string.google_places_search_server_key);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                InputStream is = null;
                try {
                    is = new URL(locationUrl.trim()).openStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                locationBitmap = BitmapFactory.decodeStream(is);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                imagePath = MediaStore.Images.Media.insertImage(getContentResolver(), locationBitmap, placeName, placeAddress);
                assert imagePath != null;
                sharePlace();
            }
        }.execute();
    }

    private void sharePlace() {
        if (locationBitmap != null) {
            Uri imageUri = Uri.parse(imagePath);
            if (imageUri != null) {
                Intent locationShareIntent = new Intent(Intent.ACTION_SEND);
                locationShareIntent.putExtra(Intent.EXTRA_TEXT, placeName);
                locationShareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                locationShareIntent.setType("image/*");
                locationShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (locationShareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(locationShareIntent, "Share image via"), REQUEST_SHARE);
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
        String directionUrl = MapUtil.getDirectionUrl(source, destination);
        Log.d("DirectionURL", directionUrl);
        new ExecuteDirectionsAPI(this, mMap)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, directionUrl);
    }

    private void startNavigation(LatLng dest) {
        //The intent chooser displays all the installed apps.
        String suffixCoordinatesLink = destination.latitude + "," + destination.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //build the link url
        StringBuilder link = new StringBuilder();
        link.append(PREFIX_DEFAULT_GOOGLE_NAVIGATE + suffixCoordinatesLink);
        intent.setData(Uri.parse(link.toString()));

        if (intent.resolveActivity(this.getPackageManager()) != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                anfe.printStackTrace();
            }
        } else {
            Toast.makeText(this.getApplicationContext(), "Could not start the activity", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SHARE:
                if (resultCode == Activity.RESULT_CANCELED) {
                    TopMessageManager.showSuccess("You've successfully shared " + placeName, "", TopMessage.DURATION.LONG);
                }
                break;
        }
    }
}
