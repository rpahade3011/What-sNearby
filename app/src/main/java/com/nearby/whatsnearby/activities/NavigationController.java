package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nearby.whatsnearby.BuildConfig;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.appupdater.AppUpdateHandler;
import com.nearby.whatsnearby.appupdater.UpdateListener;
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

public class NavigationController extends AppCompatActivity implements GpsStatusDetector.GpsStatusDetectorCallBack {
    private static final String LOG_TAG = "NavigationController";

    private static final long RIPPLE_DURATION = 250;

    private Toolbar toolbar;
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

    private AppUpdateHandler appUpdateHandler = null;
    private boolean isNewUpdateAvailable = false;
    private String CHANGE_LOGS = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_controller);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        gpsStatusDetector = new GpsStatusDetector(NavigationController.this);
        AppRaterUtils.appDidLaunched(NavigationController.this);
        setUpToolbar();
        setUpPulsatorLayout();
        setUpGuillotineDrawer();
        checkGpsState();
        // Check for updates
        startCheckingForNewUpdates();
    }

    private void setUpGuillotineDrawer() {
        // Guillotine Navigation Drawer initialization
        contentHamburger = findViewById(R.id.content_hamburger);
        root = (FrameLayout) findViewById(R.id.root);

        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        exploreGroup = (LinearLayout) guillotineMenu.findViewById(R.id.explore_group);
        shareGroup = (LinearLayout) guillotineMenu.findViewById(R.id.share_group);
        aboutGroup = (LinearLayout) guillotineMenu.findViewById(R.id.profile_group);
        root.addView(guillotineMenu);
        final TextView explore = (TextView) guillotineMenu.findViewById(R.id.tvExplore);
        imgVwAbout = (ImageView) guillotineMenu.findViewById(R.id.imgVwAbout);

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

        exploreGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationController.this, PlacesMain.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String transitionName = getResources().getString(R.string.transition_explore);
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(NavigationController.this, explore, transitionName);
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
                //guillotineAnimation.close();

            }
        });

        shareGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guillotineAnimation.close();
                String APPLICATION_NAME = getResources().getString(R.string.app_name);
                String APPLICATION_DESC = "Let me recommend you this application to find places your nearby." + "\n\n";
                Intent shareApplicationLink = new Intent(Intent.ACTION_SEND);
                shareApplicationLink.setType("text/plain");
                shareApplicationLink.putExtra(Intent.EXTRA_SUBJECT, APPLICATION_NAME);
                String sAux = APPLICATION_DESC + GlobalSettings.WHATS_NEARBY_GOOGLE_PLAY_STORE_URL_LINK;
                shareApplicationLink.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(shareApplicationLink, "Share link:"));
            }
        });

        aboutGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationController.this, ProfileActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String transitionName = getResources().getString(R.string.transition_profile);
                    final Pair<View, String>[] pairs = TransitionHelper.createSafeTransitionParticipants(NavigationController.this, false,
                            new Pair<>(imgVwAbout, transitionName));
                    ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(NavigationController.this, pairs);
                    startActivity(intent, transitionActivityOptions.toBundle());
                } else {
                    startActivity(intent);
                }
                //guillotineAnimation.close();
            }
        });
    }

    private void setUpPulsatorLayout() {
        // Pulsator layout animation
        pulsatorLayout = (PulsatorLayout) findViewById(R.id.pulsator);
        imgWorld = (ImageView) findViewById(R.id.imgWorld);
        if (pulsatorLayout != null) {
            pulsatorLayout.start();
        }
    }

    private void setUpToolbar() {
        // Setting toolbar and bottom navigation view
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(null);
            tvTitle.setText(getResources().getString(R.string.app_name));
        }
        // Setting navigation bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    public static void checkGpsState() {
        gpsStatusDetector.checkGpsStatus();
    }

    private void initialiseAdView(View guillotineMenu) {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(NavigationController.this, getResources().getString(R.string.wn_banner_id));

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        fAdView = (AdView) guillotineMenu.findViewById(R.id.ad_view);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."

        // Added code on 06-Jan-2017, by Rudraksh

        // Check whether our application is in "DEBUG" mode.
        // We need to load test ads on our physical devices.
        if (BuildConfig.DEBUG) {
            String deviceIdForTestAds = Utils.getAdMobDeviceId(NavigationController.this);
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
        if (appUpdateHandler == null) {
            Log.e(LOG_TAG, "Start checking for updates");
            appUpdateHandler = new AppUpdateHandler(NavigationController.this);
            // to start version checker
            appUpdateHandler.startCheckingUpdate();
            // prompting intervals
            appUpdateHandler.setCount(1);
            // to print new features added automatically
            appUpdateHandler.setWhatsNew(true);
            // listener for custom update prompt
            appUpdateHandler.setOnUpdateListener(new UpdateListener() {
                @Override
                public void onUpdateFound(boolean newVersion, String whatsNew) {
                    Log.e(LOG_TAG, "New updates found - " + newVersion + " : " + whatsNew);
                    isNewUpdateAvailable = newVersion;
                    CHANGE_LOGS = whatsNew;
                    compareUpdates();
                }
            });
        }
    }

    private void compareUpdates() {
        // Added code on 08-March-2017, by Rudraksh
        if (isNewUpdateAvailable && !CHANGE_LOGS.equals("")) {
            if (appUpdateHandler != null) {
                // Display update dialog
                appUpdateHandler.showDefaultAlert(true);
            }
        } else {
            Log.e(LOG_TAG, "No updates found.");
        }
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
