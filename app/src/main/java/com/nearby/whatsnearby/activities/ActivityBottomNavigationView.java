package com.nearby.whatsnearby.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.rudraksh.inappupdate.InAppUpdateManager;
import com.android.rudraksh.inappupdate.UpdateType;
import com.android.rudraksh.inappupdate.UserResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.fragments.about.FragmentAbout;
import com.nearby.whatsnearby.fragments.explore.FragmentExplore;
import com.nearby.whatsnearby.fragments.home.FragmentHome;
import com.nearby.whatsnearby.interfaces.GpsStatusDetector;
import com.nearby.whatsnearby.services.MonitorService;
import com.nearby.whatsnearby.utilities.AppRaterUtils;
import com.nearby.whatsnearby.utilities.Utils;

public class ActivityBottomNavigationView extends AppCompatActivity
        implements GpsStatusDetector.GpsStatusDetectorCallBack {

    private static final String LOG_TAG = "BottomNavigation";
    private GpsStatusDetector gpsStatusDetector = null;
    private InAppUpdateManager mInAppUpdaterManager;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nagivation_controller);

        gpsStatusDetector = new GpsStatusDetector(this);
        AppRaterUtils.appDidLaunched(this);

        setUpToolbar();

        checkGpsState();

        startServices();

        // Check for updates
        startCheckingForNewUpdates();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(FragmentHome.newInstance());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_privacy_policy) {
            Intent openIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getResources().getString(R.string.dev_privacy_policy_url)));
            if (openIntent.resolveActivity(getPackageManager()) != null) {
                try {
                    startActivity(Intent.createChooser(openIntent, "Open via"));
                } catch (ActivityNotFoundException anfe) {
                    anfe.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "No such app found to view this content", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Replaces the current screen according to user selection.
     * @param fragment
     */
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setUpToolbar() {
        // Setting toolbar and bottom navigation view
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
        // Setting navigation bar color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.text_secondary));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.text_secondary));
        toolbar.setBackgroundColor(getResources().getColor(R.color.text_secondary));
    }

    private void checkGpsState() {
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

    private void startServices() {
        // GPSService
        Intent monitorService = new Intent(this, MonitorService.class);
        monitorService.putExtra("inputExtra", getResources().getString(R.string.app_name)
                + " is monitoring your GPS & Network status");
        if (Utils.getInstance().isOreoOrLater()) {
            ContextCompat.startForegroundService(this, monitorService);
        } else {
            startService(monitorService);
        }
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

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        openFragment(FragmentHome.newInstance());
                        return true;
                    case R.id.navigation_explore:
                        openFragment(FragmentExplore.newInstance());
                        return true;
                    case R.id.navigation_about:
                        openFragment(FragmentAbout.newInstance());
                        return true;
                }
                return false;
            };
}