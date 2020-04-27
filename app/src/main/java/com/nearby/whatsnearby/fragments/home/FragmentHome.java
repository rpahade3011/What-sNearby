package com.nearby.whatsnearby.fragments.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nearby.whatsnearby.BuildConfig;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.utilities.Utils;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class FragmentHome extends Fragment {
    private static final String LOG_TAG = "FragmentHome";
    private static final long PULSATOR_DURATION = 8000;

    private PulsatorLayout mPulsatorLayout;

    private AdView fAdView = null;
    private HomePresenter mHomePresenter;

    private Handler mPulsatorHandler;
    private Runnable mPulsatorRunnable;

    public FragmentHome() {

    }

    public static FragmentHome newInstance() {
        return new FragmentHome();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mHomePresenter = new HomePresenter(mHomeView);
        return inflater.inflate(R.layout.fragment_home, container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mHomePresenter.setup(view);
        mHomePresenter.initializeAds(view);
    }

    private void startPulsator() {
        if (mPulsatorLayout != null) {
            mPulsatorLayout.start();
        }
        resetPulsator();
    }

    private void stopPulsator() {
        if (mPulsatorLayout != null) {
            mPulsatorLayout.stop();
        }
    }

    private void resetPulsator() {
        mPulsatorHandler = new Handler();
        mPulsatorHandler.postDelayed(mPulsatorRunnable, PULSATOR_DURATION);
        mPulsatorRunnable = () -> {
            stopPulsator();
            startPulsator();
        };
    }

    private final HomeView mHomeView = new HomeView() {
        @Override
        public void notifyUIReady(View rootView) {
            mPulsatorLayout = rootView.findViewById(R.id.pulsator);
            startPulsator();
        }

        @Override
        public void setupAds(View rootView) {
            // Initialize the Mobile Ads SDK.
            MobileAds.initialize(getActivity(),
                    getResources().getString(R.string.wn_banner_id));

            fAdView = rootView.findViewById(R.id.ad_view);

            if (BuildConfig.DEBUG) {
                String deviceIdForTestAds = Utils.getInstance().getAdMobDeviceId(getActivity());
                Log.i(LOG_TAG, "Hashed device id to load test ads - " + deviceIdForTestAds);
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
    };
}
