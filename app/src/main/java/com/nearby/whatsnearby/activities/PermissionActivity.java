package com.nearby.whatsnearby.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.permissions.PermissionAdapter;
import com.nearby.whatsnearby.permissions.PermissionsPreferences;
import com.nearby.whatsnearby.permissions.PermissionsUtil;


/**
 * Created by rudraksh.pahade on 12-07-2016.
 */

public class PermissionActivity extends Activity {
    private static final String TAG = PermissionActivity.class.getSimpleName();
    /**
     * Id to identify all permissions request.
     */
    private static final int REQUEST_PERMISSIONS = 1;
    /**
     * Permissions required to access location. Used by the .
     */
    private static String[] PERMISSIONS_LIST = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE};
    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;
    private LayoutInflater mDialogInflater;

    /**
     * String variable to be displayed on screen;
     */
    final String SETUP_TEXT_VALUE = "Setting up application. Please wait...";
    final String CHECK_PERM_TEXT_VALUE = "Gathering permissions. Please wait...";
    final String START_APP_TEXT_VALUE = "Starting application. Please wait...";

    /**
     * TextView to display current processing.
     */
    private TextView tvPermissionName;

    private PermissionsPreferences permissionsPreferences = new PermissionsPreferences();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window w = getWindow();
        w.setFlags(
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(Color.parseColor("#FFFFFF"));

        setContentView(R.layout.activity_permissions);

        mLayout = findViewById(R.id.permissionMain);
        tvPermissionName = findViewById(R.id.tvPermissionName);

        tvPermissionName.setText(SETUP_TEXT_VALUE);

        // Start animating our ImageViews
        setUpGears();
        new Handler().postDelayed(() -> {
            // Checking required permissions are granted
            Log.i(TAG, "onCreate(). Gathering permissions");
            checkPermissions(PermissionActivity.this);
        }, 2000);
    }

    private void setUpGears() {
        final ImageView gearProgressLeft = findViewById(R.id.gear_progress_left);
        final ImageView gearProgressRight = findViewById(R.id.gear_progress_right);

        final RotateAnimation gearProgressLeftAnim = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        gearProgressLeftAnim.setRepeatCount(Animation.INFINITE);
        gearProgressLeftAnim.setDuration((long) 2 * 1500);
        gearProgressLeftAnim.setInterpolator(new LinearInterpolator());

        final RotateAnimation gearProgressRightAnim = new RotateAnimation(360.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        gearProgressRightAnim.setRepeatCount(Animation.INFINITE);
        gearProgressRightAnim.setDuration((long) 1500);
        gearProgressRightAnim.setInterpolator(new LinearInterpolator());

        gearProgressLeft.post(() -> gearProgressLeft.setAnimation(gearProgressLeftAnim));

        gearProgressLeft.post(() -> gearProgressRight.setAnimation(gearProgressRightAnim));
    }

    private void checkPermissions(Context context) {
        if (!permissionsPreferences.getPermissionPreferences(context)) {
            tvPermissionName.setText(CHECK_PERM_TEXT_VALUE);
            Log.i(TAG, "Permissions has NOT been granted. Requesting permission.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example if the user has previously denied the permission.
                Log.i(TAG,
                        "Displaying location permission rationale to provide additional context.");
                Snackbar.make(mLayout, R.string.permission_location_rationale,
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", view ->
                        inflatePermissionDialog()).show();
            } else {
                // Permission has not been granted yet. Request it directly.
                new Handler().postDelayed(() -> {
                    // Checking required permissions are granted
                    Log.i(TAG, "checkPermissions(). Asking permissions");
                    inflatePermissionDialog();
                }, 2000);
            }
        } else {
            tvPermissionName.setText(START_APP_TEXT_VALUE);
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                navigateToNavigationActivity();
            }).start();

        }
    }

    private void inflatePermissionDialog() {
        mDialogInflater = getLayoutInflater();
        // Inflating custom dialog title view
        View customTitleView = mDialogInflater.inflate(R.layout.perm_dialog_custom_title, null);

        // All list of permissions are not granted, display an AlertDialog
        AlertDialog.Builder permissionDialogBuilder = new AlertDialog.Builder(PermissionActivity.this);

        permissionDialogBuilder.setIcon(R.mipmap.ic_launcher);
        permissionDialogBuilder.setCancelable(false);
        permissionDialogBuilder.setCustomTitle(customTitleView);
        permissionDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            doAskPermissions();
        });

        // Inflating dialog ListView
        View permDialogView = mDialogInflater.inflate(R.layout.layout_permission_dialog, null);
        permissionDialogBuilder.setView(permDialogView);

        // Extracting ListView from {@link layout_permission_dialog}
        ListView permissionListView = permDialogView.findViewById(R.id.exLstPerm);

        PermissionAdapter permissionDialogAdapter = new PermissionAdapter(PermissionActivity.this);

        permissionListView.setAdapter(permissionDialogAdapter);

        // Creating and displaying dialog
        AlertDialog permDialog = permissionDialogBuilder.create();
        permDialog.show();
    }

    private void doAskPermissions() {
        ActivityCompat.requestPermissions(PermissionActivity.this,
                PERMISSIONS_LIST,
                REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            // We have requested multiple permissions for this application, so all of them need to be
            // checked.
            if (PermissionsUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display map.
                if (permissionsPreferences.savePermissionPreferences(getApplicationContext(),
                        true, true, true)) {
                    tvPermissionName.setText(START_APP_TEXT_VALUE);
                    Snackbar.make(mLayout, R.string.permission_available_location,
                            Snackbar.LENGTH_SHORT).show();
                    // Checking GPS state of device

                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        navigateToNavigationActivity();

                    }).start();

                } else {
                    Log.i(TAG, "Location permissions were NOT granted.");
                    Snackbar.make(mLayout, R.string.permissions_not_granted,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }

            } else {
                Log.i(TAG, "Location permissions were NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void navigateToNavigationActivity() {
        if (!permissionsPreferences.getApplicationOk(getApplicationContext())) {
            permissionsPreferences.setApplicationOk(getApplicationContext(), true);
            //Intent intent = new Intent(PermissionActivity.this, NavigationController.class);
            Intent intent = new Intent(PermissionActivity.this,
                    ActivityBottomNavigationView.class);
            PermissionActivity.this.startActivity(intent);
            PermissionActivity.this.finish();
        }
    }
}