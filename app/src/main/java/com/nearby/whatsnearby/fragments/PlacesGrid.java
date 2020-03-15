package com.nearby.whatsnearby.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.GridView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nearby.whatsnearby.BuildConfig;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.activities.PlaceResult;
import com.nearby.whatsnearby.adapters.TilesFormatter;
import com.nearby.whatsnearby.constants.PlacesConstants;
import com.nearby.whatsnearby.utilities.GooglePlayServicesUtil;


/**
 * Created by rudhraksh.pahade on 11-07-2016.
 */

public class PlacesGrid extends Fragment {

    private InterstitialAd mInterstitialAd = null;
    private TilesFormatter tilesFormatter = null;
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.places_grid, container, false);

        final PlacesConstants placesConstants = new PlacesConstants();
        final Context context = getActivity().getApplicationContext();
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.animation_grow_in);

        GridView gridview = view.findViewById(R.id.places);
        tilesFormatter = new TilesFormatter(context);
        gridview.setAdapter(tilesFormatter);

        GridLayoutAnimationController controller
                = new GridLayoutAnimationController(animation, .2f, .2f);
        gridview.setLayoutAnimation(controller);

        gridview.setOnItemClickListener((adapterView, view1, position, l) -> {

            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            Intent intent = new Intent(context, PlaceResult.class);
            String place = placesConstants.places_list[position];
            intent.putExtra("Place_id", place);
            String transitionName = getActivity().getResources().getString(R.string.transition_grid_item);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(),
                            view1, transitionName);
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        });
        showAds();
        return view;
    }

    private void showAds() {
        if (mInterstitialAd == null) {
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(getActivity().getResources().getString(R.string.wn_interstitial_id));
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });
        }
    }

    private void requestNewInterstitial() {
        if (BuildConfig.DEBUG) {
            String deviceIdForTestAds = GooglePlayServicesUtil.getAdMobDeviceId(getActivity());
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(deviceIdForTestAds)
                    .build();
            mInterstitialAd.loadAd(adRequest);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            assert mInterstitialAd != null;
            mInterstitialAd.loadAd(adRequest);
        }
    }
}