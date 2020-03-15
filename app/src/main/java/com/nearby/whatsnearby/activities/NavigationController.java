package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.android.rudraksh.inappupdate.InAppUpdateManager;
import com.android.rudraksh.inappupdate.UpdateType;
import com.android.rudraksh.inappupdate.UserResponse;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.nearby.whatsnearby.BuildConfig;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.guillotine.GuillotineAnimation;
import com.nearby.whatsnearby.guillotine.GuillotineListener;
import com.nearby.whatsnearby.interfaces.GpsStatusDetector;
import com.nearby.whatsnearby.utilities.AppRaterUtils;
import com.nearby.whatsnearby.utilities.TransitionHelper;
import com.nearby.whatsnearby.utilities.Utils;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;


/**
 * Created by rudhraksh.pahade on 19-07-2016.
 */

public class NavigationController extends AppCompatActivity
        implements GpsStatusDetector.GpsStatusDetectorCallBack {

    private static final String LOG_TAG = "NavigationController";

    private static final long RIPPLE_DURATION = 250;

    private MaterialToolbar toolbar;
    private FrameLayout root;
    private View contentHamburger;
    private LinearLayout exploreGroup;
    private LinearLayout aboutGroup;
    private LinearLayout shareGroup;
    private PulsatorLayout pulsatorLayout;
    private ImageView imgWorld;
    private ImageView imgVwAbout;
    private TextView tvTitle;
    private GuillotineAnimation guillotineAnimation = null;
    private GuillotineAnimation.GuillotineBuilder guillotineBuilder = null;

    static GpsStatusDetector gpsStatusDetector = null;
    private AdView fAdView = null;
    private boolean isGuillotineOpened = false;

    private InAppUpdateManager mInAppUpdaterManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_controller);
        tvTitle = findViewById(R.id.tvTitle);
        gpsStatusDetector = new GpsStatusDetector(NavigationController.this);
        AppRaterUtils.appDidLaunched(NavigationController.this);
        setUpToolbar();
        setUpPulsatorLayout();
        setUpGuillotineDrawer();
        checkGpsState();
        // Check for updates
        startCheckingForNewUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInAppUpdaterManager.continueUpdate();
    }

    private void setUpGuillotineDrawer() {
        // Guillotine Navigation Drawer initialization
        contentHamburger = findViewById(R.id.content_hamburger);
        root = findViewById(R.id.root);

        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        exploreGroup = guillotineMenu.findViewById(R.id.explore_group);
        shareGroup = guillotineMenu.findViewById(R.id.share_group);
        aboutGroup = guillotineMenu.findViewById(R.id.profile_group);
        root.addView(guillotineMenu);
        final TextView explore = guillotineMenu.findViewById(R.id.tvExplore);
        imgVwAbout = guillotineMenu.findViewById(R.id.imgVwAbout);

        initialiseAdView(guillotineMenu);

        if (guillotineAnimation == null) {
            guillotineBuilder = new GuillotineAnimation.GuillotineBuilder
                    (guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger);
            guillotineBuilder.setStartDelay(RIPPLE_DURATION);
            guillotineBuilder.setActionBarViewForAnimation(toolbar);
            guillotineBuilder.setClosedOnStart(true);
            guillotineBuilder.setGuillotineListener(new GuillotineListener() {
                @Override
                public void onGuillotineOpened() {
                    isGuillotineOpened = true;
                    Log.e(LOG_TAG, "Menu opened");
                }

                @Override
                public void onGuillotineClosed() {
                    isGuillotineOpened = false;
                    Log.e(LOG_TAG, "Menu closed");
                }
            });
            guillotineBuilder.build();
            guillotineAnimation = new GuillotineAnimation(guillotineBuilder);
            guillotineAnimation.close();
        }

        exploreGroup.setOnClickListener(v -> {
            Intent intent = new Intent(NavigationController.this, PlacesMain.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String transitionName = getResources().getString(R.string.transition_explore);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(NavigationController.this, explore, transitionName);
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        });

        shareGroup.setOnClickListener(v -> {
            guillotineAnimation.close();
            String APPLICATION_NAME = getResources().getString(R.string.app_name);
            String APPLICATION_DESC = "Let me recommend you this application to find places your nearby."
                    + "\n\n";
            Intent shareApplicationLink = new Intent(Intent.ACTION_SEND);
            shareApplicationLink.setType("text/plain");
            shareApplicationLink.putExtra(Intent.EXTRA_SUBJECT, APPLICATION_NAME);
            String sAux = APPLICATION_DESC + GlobalSettings.WHATS_NEARBY_GOOGLE_PLAY_STORE_URL_LINK;
            shareApplicationLink.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(shareApplicationLink, "Share link:"));
        });

        aboutGroup.setOnClickListener(v -> {
            Intent intent = new Intent(NavigationController.this, ProfileActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String transitionName = getResources().getString(R.string.transition_profile);
                final Pair<View, String>[] pairs = TransitionHelper
                        .createSafeTransitionParticipants(NavigationController.this, false,
                        new Pair<>(imgVwAbout, transitionName));
                ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(NavigationController.this, pairs);
                startActivity(intent, transitionActivityOptions.toBundle());
            } else {
                startActivity(intent);
            }
        });
    }

    private void setUpPulsatorLayout() {
        // Pulsator layout animation
        pulsatorLayout = findViewById(R.id.pulsator);
        imgWorld = findViewById(R.id.imgWorld);
        if (pulsatorLayout != null) {
            pulsatorLayout.start();
        }
    }

    private void setUpToolbar() {
        // Setting toolbar and bottom navigation view
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(null);
            tvTitle.setText(getResources().getString(R.string.app_name));
        }
        // Setting navigation bar color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    public static void checkGpsState() {
        gpsStatusDetector.checkGpsStatus();
    }

    private void initialiseAdView(View guillotineMenu) {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(NavigationController.this,
                getResources().getString(R.string.wn_banner_id));

        fAdView = guillotineMenu.findViewById(R.id.ad_view);

        if (BuildConfig.DEBUG) {
            String deviceIdForTestAds = Utils.getInstance().getAdMobDeviceId(NavigationController.this);
            Log.e(LOG_TAG, "Hashed device id to load test ads - " + deviceIdForTestAds);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(deviceIdForTestAds).build();
            assert fAdView != null;
            fAdView.loadAd(adRequest);
        } else {
            // "RELEASE" mode
            // Load live ads
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            assert fAdView != null;
            fAdView.loadAd(adRequest);
        }
    }

    private void startCheckingForNewUpdates() {
        mInAppUpdaterManager = InAppUpdateManager.initialize(this);
        mInAppUpdaterManager.checkAvailableUpdateIfFound(versionCode -> {
            Log.i(LOG_TAG, "New version found: " + versionCode);
            mInAppUpdaterManager.popupDialogForUpdateAvailability(userResponse -> {
                if (userResponse == UserResponse.ACCEPTED) {
                    mInAppUpdaterManager.setDefaultUpdateMode(UpdateType.APP_UPDATE_TYPE_FLEXIBLE)
                            .startCheckingForUpdates();
                } else {
                    mInAppUpdaterManager.popupSnackbarForUpdateRejection();
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        gpsStatusDetector.checkOnActivityResult(requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (isGuillotineOpened) {
            guillotineAnimation.close();
        } else {
            if (GlobalSettings.BACK_PRESSED + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                NavigationController.this.finish();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.application_exit_msg,
                        getResources().getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
                GlobalSettings.BACK_PRESSED = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onGpsSettingStatus(boolean enabled) {
        Log.e(LOG_TAG, "Gps enabled: " + enabled);
        Snackbar.make(pulsatorLayout, enabled ? "GPS Enabled" : "GPS Disabled", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onGpsAlertCanceledByUser() {
        checkGpsState();
    }
}