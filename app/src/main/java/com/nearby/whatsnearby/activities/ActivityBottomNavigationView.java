package com.nearby.whatsnearby.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.nearby.whatsnearby.interfaces.IBottomNavView;
import com.nearby.whatsnearby.interfaces.IGpsStatusDetectorCallBack;
import com.nearby.whatsnearby.permissions.PermissionsPreferences;
import com.nearby.whatsnearby.presenters.BottomNavPresenter;
import com.nearby.whatsnearby.services.MonitorService;
import com.nearby.whatsnearby.utilities.AppRaterUtils;
import com.nearby.whatsnearby.utilities.GpsStatusDetector;
import com.nearby.whatsnearby.utilities.PermissionsUtil;
import com.nearby.whatsnearby.utilities.Utils;

public class ActivityBottomNavigationView extends AppCompatActivity
        implements IGpsStatusDetectorCallBack {

    private static final String LOG_TAG = "BottomNavigation";
    private GpsStatusDetector gpsStatusDetector = null;
    private InAppUpdateManager mInAppUpdaterManager;
    private BottomNavigationView navView;
    private MaterialToolbar toolbar;
    private BottomNavPresenter mPresenter;
    private Handler mFragmentHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bottom_nagivation_controller);

        mPresenter = new BottomNavPresenter(mBottomNavView);

        mPresenter.createView();
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
        mPresenter.checkGpsState();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mPresenter.launchFragment(FragmentHome.newInstance());
                        return true;
                    case R.id.navigation_explore:
                        if (PermissionsUtil.getInstance()
                                .ifPermissionsAreGranted(ActivityBottomNavigationView.this)) {
                            mPresenter.launchFragment(FragmentExplore.newInstance());
                        } else {
                            mPresenter.navigateToPermissionScreen();
                        }
                        return true;
                    case R.id.navigation_about:
                        mPresenter.launchFragment(FragmentAbout.newInstance());
                        return true;
                }
                return false;
            };

    private final IBottomNavView mBottomNavView = new IBottomNavView() {
        @Override
        public void initializeView() {
            mFragmentHandler = new Handler();
            toolbar = findViewById(R.id.toolbar);
            navView = findViewById(R.id.nav_view);
            gpsStatusDetector = new GpsStatusDetector(ActivityBottomNavigationView.this);
            AppRaterUtils.appDidLaunched(ActivityBottomNavigationView.this);
            mPresenter.setUpToolbar();
        }

        @Override
        public void setUpToolbar() {
            // Setting toolbar and bottom navigation view
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            }
            // Setting navigation bar color
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.text_secondary));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.text_secondary));
            toolbar.setBackgroundColor(getResources().getColor(R.color.text_secondary));
            mPresenter.checkGpsState();
        }

        @Override
        public void checkGpsState() {
            gpsStatusDetector.checkGpsStatus();
            mPresenter.startServices();
        }

        @Override
        public void startCheckingForNewUpdates() {
            mInAppUpdaterManager = InAppUpdateManager.initialize(ActivityBottomNavigationView.this);
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
            mPresenter.createBottomNav();
        }

        @Override
        public void startServices() {
            // GPSService
            Intent monitorService = new Intent(ActivityBottomNavigationView.this,
                    MonitorService.class);
            monitorService.putExtra("inputExtra", getResources().getString(R.string.app_name)
                    + " is monitoring your GPS & Network status");
            if (Utils.getInstance().isOreoOrLater()) {
                ContextCompat.startForegroundService(ActivityBottomNavigationView.this,
                        monitorService);
            } else {
                ActivityBottomNavigationView.this.startService(monitorService);
            }
            mPresenter.startCheckingForNewUpdates();
        }

        @Override
        public void initializeBottomNav() {
            navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
            // Checking if app is launched from app shortcuts
            String value = getIntent().getStringExtra("key_explore");
            if (value != null && value.equals("value_navigate_to_explore")) {
                mPresenter.launchFragment(FragmentExplore.newInstance());
                navView.getMenu().findItem(R.id.navigation_explore).setChecked(true);
            } else {
                mPresenter.launchFragment(FragmentHome.newInstance());
                navView.getMenu().findItem(R.id.navigation_home).setChecked(true);
            }
        }

        @Override
        public void openFragment(Fragment fragment) {
            // Sometimes, when fragment has huge data, screen seems hanging
            // when switching between navigation menus.
            // So using runnable, the fragment is loaded with cross fade effect.
            // This effect can be seen in Gmail app.
            Runnable mPendingRunnable = () -> {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.nav_host_fragment, fragment);
                transaction.commit();
            };
            mFragmentHandler.post(mPendingRunnable);
        }

        @Override
        public void navigateToPermissionScreen() {
            PermissionsPreferences.getInstance().clear(getApplicationContext());
            Intent mainIntent = new Intent(ActivityBottomNavigationView.this,
                    ActivityPermissions.class);
            ActivityBottomNavigationView.this.startActivity(mainIntent);
            ActivityBottomNavigationView.this.finish();
        }
    };
}