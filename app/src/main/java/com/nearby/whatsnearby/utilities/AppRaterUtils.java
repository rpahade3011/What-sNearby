package com.nearby.whatsnearby.utilities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    public static void showRateDialog(final AppCompatActivity mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_app_rate);

        final Button btnRateMe = (Button) dialog.findViewById(R.id.buttonRateApp);
        final Button btnRemindLater = (Button) dialog.findViewById(R.id.buttonRemindLater);
        final Button btnNoThanks = (Button)dialog.findViewById(R.id.buttonNoThanks);

        TextView tvAppRateMessage = (TextView) dialog.findViewById(R.id.tvAppRateMessage);
        tvAppRateMessage.setText(mContext.getResources().getString(R.string.app_rate_text,
                mContext.getResources().getString(R.string.app_name)));

        // Button Rate Clicked
        btnRateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                }catch (Exception e){
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id="+ APP_PNAME)));
                }

                dialog.dismiss();
            }
        });
        // Button Remind Later Clicked
        btnRemindLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Button No Thanks Clicked
        btnNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        if (dialog != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.RateDialogTheme;
            dialog.show();
        }
    }
}
