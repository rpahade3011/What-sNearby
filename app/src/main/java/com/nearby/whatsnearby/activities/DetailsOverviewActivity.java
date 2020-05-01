package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.BottomSheetPagerAdapter;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.fragments.overview.OverviewFragment;
import com.nearby.whatsnearby.fragments.photos.PhotosFragment;
import com.nearby.whatsnearby.fragments.review.ReviewFragment;
import com.nearby.whatsnearby.services.AppController;

public class DetailsOverviewActivity extends AppCompatActivity {

    private final static String LOG_TAG = "DetailsOverviewActivity";

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_details);

        getIntentData();

        ImageView close = findViewById(R.id.btn_close);
        TextView title = findViewById(R.id.title);

        title.setText(mDataPlaceName);

        close.setOnClickListener(v -> {
            onBackPressed();
        });

        final ViewPager viewPager = findViewById(R.id.htab_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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

    private void setupViewPager(ViewPager viewPager) {
        BottomSheetPagerAdapter adapter = new BottomSheetPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(OverviewFragment.getInstance(getOverviewFragmentData()), "Overview");
        adapter.addFragment(ReviewFragment.getInstance(), "Review");
        adapter.addFragment(PhotosFragment.getInstance(), "Photos");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }

    private Bundle getOverviewFragmentData() {
        Bundle bundle = new Bundle();
        bundle.putString("Name", mDataPlaceName);
        bundle.putString("Address", mDataPlaceAddress);
        bundle.putBoolean("Timing", mDataIsOpen);
        bundle.putString("CompoundAddress", mDataCompoundAddress);
        bundle.putString("ContactNumber", mDataContactNumber);
        bundle.putString("Place_Website", mDataWebsiteUrl);
        return bundle;
    }
}

