package com.nearby.whatsnearby.fragments.about;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nearby.whatsnearby.BuildConfig;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.utilities.Utils;
import com.vansuita.materialabout.builder.AboutBuilder;

public class FragmentAbout extends Fragment {

    private static final String TAG = "FragmentAbout";
    private AdView fAdView = null;

    public FragmentAbout() {

    }

    public static FragmentAbout newInstance() {
        return new FragmentAbout();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initialiseAdView(view);
        loadAboutMe(view);
    }

    private void initialiseAdView(View view) {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(getActivity(), getActivity().getResources().getString(R.string.wn_banner_id));

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        fAdView = view.findViewById(R.id.ad_view);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."

        // Added code on 06-Jan-2017, by Rudraksh

        // Check whether our application is in "DEBUG" mode.
        // We need to load test ads on our physical devices.
        if (BuildConfig.DEBUG) {
            String deviceIdForTestAds = Utils.getInstance().getAdMobDeviceId(getActivity());
            Log.e(TAG, "Hashed device id to load test ads - " + deviceIdForTestAds);
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

    private void loadAboutMe(View view) {
        final FrameLayout flHolder = view.findViewById(R.id.aboutme);
        if (flHolder != null) {
            flHolder.addView(
                    AboutBuilder.with(getActivity())
                            .setAppIcon(R.mipmap.ic_launcher)
                            .setAppName(R.string.app_name)
                            .setAppTitle(R.string.app_desc)
                            .setPhoto(R.mipmap.about_dev)
                            .setCover(R.drawable.nb_mainbg)
                            .setLinksAnimated(false)
                            .setDividerDashGap(13)
                            .setName("Rudraksh Pahade")
                            .setNameColor(R.color.colorAccent)
                            .setSubTitle("Mobile Developer")
                            .setSubTitleColor(R.color.colorAccent)
                            .setLinksColumnsCount(3)
                            .setBrief("I'm warmed of mobile technologies. Ideas maker, curious and nature lover.")
                            .addGooglePlayStoreLink("6251285879755104834")
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
                            .setActionsColumnsCount(3)
                            .addFeedbackAction("rudraksh3011@gmail.com")
                            .build());
        }
    }
}