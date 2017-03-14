package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.guillotine.GuillotineAnimation;
import com.nearby.whatsnearby.interfaces.GpsStatusDetector;

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
    private PulsatorLayout pulsatorLayout;
    private ImageView imgWorld;
    private TextView tvTitle;
    private GuillotineAnimation guillotineAnimation = null;
    private GuillotineAnimation.GuillotineBuilder guillotineBuilder = null;

    private SweetAlertDialog sweetAlertDialog = null;
    static GpsStatusDetector gpsStatusDetector = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_controller);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        gpsStatusDetector = new GpsStatusDetector(NavigationController.this);
        setUpToolbar();
        setUpPulsatorLayout();
        setUpGuillotineDrawer();
        checkGpsState();
    }

    private void setUpGuillotineDrawer() {
        // Guillotine Navigation Drawer initialization
        contentHamburger = findViewById(R.id.content_hamburger);
        root = (FrameLayout) findViewById(R.id.root);

        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        exploreGroup = (LinearLayout) guillotineMenu.findViewById(R.id.explore_group);
        aboutGroup = (LinearLayout) guillotineMenu.findViewById(R.id.profile_group);
        root.addView(guillotineMenu);

        if (guillotineAnimation == null) {
            guillotineBuilder = new GuillotineAnimation.GuillotineBuilder
                    (guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger);
            guillotineBuilder.setStartDelay(RIPPLE_DURATION);
            guillotineBuilder.setActionBarViewForAnimation(toolbar);
            guillotineBuilder.setClosedOnStart(true);
            guillotineBuilder.build();
            guillotineAnimation = new GuillotineAnimation(guillotineBuilder);
            guillotineAnimation.close();
        }

        exploreGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guillotineAnimation.close();
                Intent intent = new Intent(NavigationController.this, PlacesMain.class);
                startActivity(intent);

            }
        });
        aboutGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guillotineAnimation.close();
                Intent intent = new Intent(NavigationController.this, ProfileActivity.class);
                startActivity(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        gpsStatusDetector.checkOnActivityResult(requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
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
