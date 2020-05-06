package com.nearby.whatsnearby.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.interfaces.IPermissionView;
import com.nearby.whatsnearby.permissions.PermissionsPreferences;
import com.nearby.whatsnearby.presenters.PermissionPresenter;
import com.nearby.whatsnearby.utilities.PermissionsUtil;

public class ActivityPermissions extends AppCompatActivity {

    private static final String TAG = ActivityPermissions.class.getSimpleName();
    /**
     * Id to identify all permissions request.
     */
    private static final int REQUEST_PERMISSIONS = 1;
    /**
     * Permissions required to access location. Used by the .
     */
    private static String[] PERMISSIONS_LIST = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE };

    private PermissionPresenter mPresenter;

    private Button btnAllowLocation, btnAllowMedia, btnAllowPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_permissions_screen);
        mPresenter = new PermissionPresenter(mPermissionView);
        mPresenter.initUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            // We have requested multiple permissions for this application, so all of them need to be
            // checked.
            if (PermissionsUtil.getInstance().verifyPermissions(grantResults)) {
                // All required permissions have been granted, display map.
                if (PermissionsPreferences.getInstance().savePermissionPreferences(getApplicationContext(),
                        true, true, true)) {
                    Snackbar.make(getRootView(), R.string.permission_available_location,
                            Snackbar.LENGTH_SHORT).show();
                    // Checking GPS state of device

                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mPresenter.navigateToMainScreen();

                    }).start();

                } else {
                    Log.i(TAG, "Location permissions were NOT granted.");
                    Snackbar.make(getRootView(), R.string.permissions_not_granted,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }

            } else {
                Log.i(TAG, "Location permissions were NOT granted.");
                Snackbar.make(getRootView(), R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private View getRootView() {
        return getWindow().getDecorView();
    }

    private void checkPermissions(Context context) {
        if (!PermissionsPreferences.getInstance().getPermissionPreferences(context)) {
            Log.i(TAG, "Permissions has NOT been granted. Requesting permission.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example if the user has previously denied the permission.
                Log.i(TAG,
                        "Displaying location permission rationale to provide additional context.");
                Snackbar snackbar = Snackbar.make(getRootView(), R.string.permission_location_rationale,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setTextColor(Color.parseColor("#212121"));
                snackbar.setActionTextColor(Color.parseColor("#E98A15"));
                snackbar.setAction("OK", view ->
                        mPresenter.askPermissionsToGrant());
                snackbar.show();
            } else {
                // Permission has not been granted yet. Request it directly.
                new Handler().postDelayed(() -> {
                    // Checking required permissions are granted
                    Log.i(TAG, "checkPermissions(). Asking permissions");
                    mPresenter.askPermissionsToGrant();
                }, 2000);
            }
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mPresenter.navigateToMainScreen();
            }).start();

        }
    }

    private void navigateToNavigationActivity() {
        if (!PermissionsPreferences.getInstance().getApplicationOk(getApplicationContext())) {
            PermissionsPreferences.getInstance().setApplicationOk(getApplicationContext(), true);
            Intent intent = new Intent(ActivityPermissions.this,
                    ActivityBottomNavigationView.class);
            ActivityPermissions.this.startActivity(intent);
            ActivityPermissions.this.finish();
        }
    }

    private final IPermissionView mPermissionView = new IPermissionView() {
        @Override
        public void notifyUIReady() {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            // Setting navigation bar color for lollipop devices
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
            btnAllowLocation = findViewById(R.id.btnAllowLocation);
            btnAllowMedia = findViewById(R.id.btnAllowMedia);
            btnAllowPhone = findViewById(R.id.btnAllowPhone);

            btnAllowLocation.setOnClickListener(v -> {
                checkPermissions(ActivityPermissions.this);
            });

            btnAllowMedia.setOnClickListener(v -> {
                checkPermissions(ActivityPermissions.this);
            });

            btnAllowPhone.setOnClickListener(v -> {
                checkPermissions(ActivityPermissions.this);
            });
        }

        @Override
        public void grantPermissions() {
            ActivityCompat.requestPermissions(ActivityPermissions.this,
                    PERMISSIONS_LIST,
                    REQUEST_PERMISSIONS);
        }

        @Override
        public void navigateToHomeScreen() {
            navigateToNavigationActivity();
        }
    };
}