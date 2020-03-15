package com.nearby.whatsnearby.utilities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nearby.whatsnearby.R;

/**
 * Created by rudhraksh.pahade on 4/17/2017.
 */

public class AppRaterUtils {
    private final static String APP_TITLE = "What's Nearby";
    private final static String APP_PNAME = "com.nearby.whatsnearby";
    private final static int DAYS_UNTIL_PROMPT = 1;
    private final static int LAUNCHES_UNTIL_PROMPT = 3;

    /**
     * Method to initialize and setup the shared prefs values,
     * get the system date and installed date of application, wait for n days
     * the call the {@link AppRaterUtils.showRateDialog()}
     * @param appCompatActivity - AppCompatActivity
     */
    public static void appDidLaunched(AppCompatActivity appCompatActivity) {
        SharedPreferences prefs = appCompatActivity.getSharedPreferences("whatsnearby_apprater", 0);

        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(appCompatActivity, editor);
            }
        }

        editor.apply();
    }

    private static void showRateDialog(final AppCompatActivity mContext,
                                      final SharedPreferences.Editor editor) {
        final AlertDialog.Builder appRaterDialogBuilder = new AlertDialog.Builder(mContext);
        appRaterDialogBuilder.setTitle("Rate " + APP_TITLE);
        appRaterDialogBuilder.setCancelable(false);
        appRaterDialogBuilder.setMessage(mContext.getResources().getString(R.string.app_rate_text,
                mContext.getResources().getString(R.string.app_name)));
        // Button Rate Clicked
        appRaterDialogBuilder.setPositiveButton(mContext.getResources()
                .getString(R.string.app_rate_rate_now_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try{
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                }catch (Exception e){
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id="+ APP_PNAME)));
                }

                dialogInterface.dismiss();
            }
        });
        // Button No Thanks Clicked
        appRaterDialogBuilder.setNegativeButton(mContext.getResources()
                .getString(R.string.app_rate_no_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialogInterface.dismiss();
            }
        });
        // Button Remind Later Clicked
        appRaterDialogBuilder.setNeutralButton(mContext.getResources()
                .getString(R.string.app_rate_remind_later_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog appRaterDialog = appRaterDialogBuilder.create();
        if (appRaterDialog != null) {
            appRaterDialog.getWindow().getAttributes().windowAnimations = R.style.RateDialogTheme;
            appRaterDialog.show();
        }
    }
}