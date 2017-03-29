package com.nearby.whatsnearby.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    private static final int DELAY = 100;
    private AdView fAdView = null;
    private Interpolator interpolator;
    private Toolbar profileToolbar;
    private RelativeLayout bgViewGroup;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        bgViewGroup = (RelativeLayout) findViewById(R.id.bgViewGroup);
        setTheme(theme);
        setupWindowAnimations();
        setupToolbar();
        initialiseAdView();
        loadAboutMe();
    }

    private void setupWindowAnimations() {
        interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in);
        setupEnterAnimations();
        setupExitAnimations();
    }

    private void setupToolbar() {
        profileToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(profileToolbar);
        if (profileToolbar != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
            profileToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        // Setting navigation bar color for lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @SuppressLint("NewApi")
    private void setupEnterAnimations() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion);
        getWindow().setSharedElementEnterTransition(transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                // Removing listener here is very important because shared element transition is executed again backwards on exit. If we don't remove the listener this code will be triggered again.
                transition.removeListener(this);
                hideTarget();
                animateRevealShow(profileToolbar);
                animateButtonsIn();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    @SuppressLint("NewApi")
    private void setupExitAnimations() {
        Fade returnTransition = new Fade();
        getWindow().setReturnTransition(returnTransition);
        returnTransition.setDuration(getResources().getInteger(R.integer.anim_duration_medium));
        returnTransition.setStartDelay(getResources().getInteger(R.integer.anim_duration_medium));
        returnTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                transition.removeListener(this);
                animateButtonsOut();
                animateRevealHide(bgViewGroup);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    private void hideTarget() {
        findViewById(R.id.shared_target).setVisibility(View.GONE);
        final TextView title = (TextView) findViewById(R.id.title);
        title.setVisibility(View.VISIBLE);
        title.setText("About");
    }

    private void animateButtonsIn() {
        for (int i = 0; i < bgViewGroup.getChildCount(); i++) {
            View child = bgViewGroup.getChildAt(i);
            child.animate()
                    .setStartDelay(100 + i * DELAY)
                    .setInterpolator(interpolator)
                    .alpha(1)
                    .scaleX(1)
                    .scaleY(1);
        }
    }

    private void animateButtonsOut() {
        for (int i = 0; i < bgViewGroup.getChildCount(); i++) {
            View child = bgViewGroup.getChildAt(i);
            child.animate()
                    .setStartDelay(i)
                    .setInterpolator(interpolator)
                    .alpha(0)
                    .scaleX(0f)
                    .scaleY(0f);
        }
    }
    @SuppressLint("NewApi")
    private void animateRevealShow(View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int finalRadius = Math.max(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);
        viewRoot.setVisibility(View.VISIBLE);
        anim.setDuration(getResources().getInteger(R.integer.anim_duration_long));
        anim.setInterpolator(new AccelerateInterpolator());
        anim.start();
    }

    @SuppressLint("NewApi")
    private void animateRevealHide(final View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int initialRadius = viewRoot.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                viewRoot.setVisibility(View.INVISIBLE);
            }
        });
        anim.setDuration(getResources().getInteger(R.integer.anim_duration_medium));
        anim.start();
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

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
