package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nearby.whatsnearby.BuildConfig;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.utilities.Utils;
import com.vansuita.materialabout.builder.AboutBuilder;

/**
 * Created by rudhraksh.pahade on 25-07-2016.
 */

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ProfileActivity";
    private static final int theme = R.style.AppThemeLight;
    private AdView fAdView = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTheme(theme);
        initialiseAdView();
        loadAboutMe();
    }

    private void initialiseAdView() {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(ProfileActivity.this, getResources().getString(R.string.wn_banner_id));

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        fAdView = (AdView) findViewById(R.id.ad_view);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."

        // Added code on 06-Jan-2017, by Rudraksh

        // Check whether our application is in "DEBUG" mode.
        // We need to load test ads on our physical devices.
        if (BuildConfig.DEBUG) {
            String deviceIdForTestAds = Utils.getAdMobDeviceId(ProfileActivity.this);
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

    private void loadAboutMe() {
        final FrameLayout flHolder = (FrameLayout) findViewById(R.id.aboutme);

        if (flHolder != null) {
            flHolder.addView(
                    AboutBuilder.with(this)
                            .setAppIcon(R.mipmap.ic_launcher)
                            .setAppName(R.string.app_name)
                            .setAppTitle(R.string.app_desc)
                            .setPhoto(R.mipmap.about_rudraksh_pahade_profile_pic)
                            .setCover(R.drawable.nb_mainbg)
                            .setLinksAnimated(false)
                            .setDividerDashGap(13)
                            .setName("Rudraksh Pahade")
                            .setNameColor(R.color.colorPrimary)
                            .setSubTitle("Mobile Developer")
                            .setLinksColumnsCount(3)
                            .setBrief("I'm warmed of mobile technologies. Ideas maker, curious and nature lover.")
                            .addGooglePlayStoreLink("109312616470328191163")
                            .addGitHubLink("rpahade3011")
                            .addFacebookLink("rudraksh.pahade")
                            .addTwitterLink("pahade_rudraksh")
                            .addInstagramLink("rudrakshpahade")
                            .addGooglePlusLink("109312616470328191163")
                            .addLinkedInLink("rudraksh-pahade-752b3b3a")
                            .addEmailLink("rudraksh3011@gmail.com")
                            .addWhatsappLink("Rudraksh", "+919028411974")
                            .addSkypeLink("rudraksh.pahade")
                            .addGoogleLink("rudraksh3011")
                            .addFiveStarsAction()
                            .addMoreFromMeAction("Rudraksh+Pahade")
                            .setVersionNameAsAppSubTitle()
                            .addShareAction(R.string.app_name)
                            .addUpdateAction()
                            .setActionsColumnsCount(2)
                            .addFeedbackAction("rudraksh3011@gmail.com")
                            .addChangeLogAction((Intent) null)
                            .build());
        }
    }
}
