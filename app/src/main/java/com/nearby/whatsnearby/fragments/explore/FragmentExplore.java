package com.nearby.whatsnearby.fragments.explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.nearby.whatsnearby.utilities.Utils;

public class FragmentExplore extends Fragment {
    private static final String LOG_TAG = "FragmentExplore";

    private ExplorePresenter mPresenter;
    private Context mContext;
    private InterstitialAd mInterstitialAd = null;
    private TilesFormatter tilesFormatter = null;
    private PlacesConstants placesConstants;

    private GridView gridview;

    public FragmentExplore() {

    }

    public static Fragment newInstance() {
        return new FragmentExplore();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate()");
        mContext = getActivity().getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView()");
        return inflater.inflate(R.layout.places_grid, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(LOG_TAG, "onViewCreated()");
        mPresenter = new ExplorePresenter(mExploreView);
        mPresenter.initializeViews(view);
        mPresenter.initializePlaces();
    }

    private void showAds() {
        if (mInterstitialAd == null) {
            mInterstitialAd = new InterstitialAd(mContext);
            mInterstitialAd.setAdUnitId(mContext.getResources()
                    .getString(R.string.wn_interstitial_id));
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
            String deviceIdForTestAds = Utils.getInstance().getAdMobDeviceId(getActivity());
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

    private final IExploreView mExploreView = new IExploreView() {
        @Override
        public void onNotifyUIReady(View view) {
            gridview = view.findViewById(R.id.places);
            tilesFormatter = new TilesFormatter(mContext);
            placesConstants = new PlacesConstants();
        }

        @Override
        public void setUpPlaces() {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.animation_grow_in);
            gridview.setAdapter(tilesFormatter);

            GridLayoutAnimationController controller
                    = new GridLayoutAnimationController(animation, .2f, .2f);
            gridview.setLayoutAnimation(controller);
            showAds();
            gridview.setOnItemClickListener((adapterView, view1, position, l) -> {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                mPresenter.gotoPlace(view1, position);
            });
        }

        @Override
        public void navigateToPlace(View view, int placePosition) {
            Intent intent = new Intent(mContext, PlaceResult.class);
            String place = placesConstants.places_list[placePosition];
            intent.putExtra("Place_id", place);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String transitionName = mContext.getResources().getString(R.string.transition_grid_item);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(),
                            view, transitionName);
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        }
    };
}