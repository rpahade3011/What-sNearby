package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.rudraksh.inappupdate.InAppUpdateManager;
import com.android.rudraksh.inappupdate.UpdateType;
import com.android.rudraksh.inappupdate.UserResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.interfaces.GpsStatusDetector;
import com.nearby.whatsnearby.utilities.AppRaterUtils;

public class ActivityBottomNavigationView extends AppCompatActivity
        implements GpsStatusDetector.GpsStatusDetectorCallBack {

    private static final String LOG_TAG = "BottomNavigation";
    private GpsStatusDetector gpsStatusDetector = null;
    private InAppUpdateManager mInAppUpdaterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nagivation_controller);

        // Setting navigation bar color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));

        gpsStatusDetector = new GpsStatusDetector(this);
        AppRaterUtils.appDidLaunched(this);

        checkGpsState();
        // Check for updates
        startCheckingForNewUpdates();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_explore, R.id.navigation_about)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInAppUpdaterManager.continueUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        gpsStatusDetector.checkOnActivityResult(requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (GlobalSettings.BACK_PRESSED + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            ActivityBottomNavigationView.this.finish();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.application_exit_msg,
                    getResources().getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
            GlobalSettings.BACK_PRESSED = System.currentTimeMillis();
        }
    }

    public void checkGpsState() {
        gpsStatusDetector.checkGpsStatus();
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

    private View getRootView() {
        return getWindow().getDecorView();
    }

    @Override
    public void onGpsSettingStatus(boolean enabled) {
        Log.e(LOG_TAG, "Gps enabled: " + enabled);
        Snackbar.make(getRootView().findViewById(android.R.id.content)
                , enabled ? "GPS Enabled" : "GPS Disabled", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onGpsAlertCanceledByUser() {
        checkGpsState();
    }
}