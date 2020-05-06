package com.nearby.whatsnearby.fragments.overview;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.activities.ActivityCall;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.fragments.error.ErrorFragment;
import com.nearby.whatsnearby.interfaces.IStaticMapsListener;
import com.nearby.whatsnearby.requests.NetworkTask;
import com.nearby.whatsnearby.utilities.MapUtil;

public class OverviewFragment extends Fragment {
    private static final String LOG_TAG = "OverviewFragment";
    private static final int REQUEST_SHARE = 1010;

    private String mDataPlaceName;
    private String mDataCompoundAddress;
    private String mDataContactNumber;
    private String mDataPlaceAddress;
    private String mDataWebsiteUrl;
    private boolean mDataIsOpen;

    public OverviewFragment() { }

    public static OverviewFragment getInstance(Bundle bundle) {
        OverviewFragment fragment = new OverviewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getIntentData(getArguments());
        return inflater.inflate(R.layout.layout_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void getIntentData(Bundle bundle) {
        if (bundle != null) {
            try {
                mDataPlaceName = bundle.getString("Name");
                mDataPlaceAddress = bundle.getString("Address");
                mDataIsOpen = bundle.getBoolean("Timing");
                mDataCompoundAddress = bundle.getString("CompoundAddress");
                mDataContactNumber = bundle.getString("ContactNumber");
                mDataWebsiteUrl = bundle.getString("Place_Website");
            } catch (Exception ex) {
                Log.e(LOG_TAG, "Exception while retrieving intent data --> "
                        + ex.getLocalizedMessage());
            }
        }
    }

    private void init(View view) {
        ImageButton buttonCall = view.findViewById(R.id.buttonCall);
        ImageButton buttonNavigate = view.findViewById(R.id.buttonNavigate);
        ImageButton buttonShare = view.findViewById(R.id.buttonShare);
        TextView placeAddressTv = view.findViewById(R.id.places_detail_address_detail);
        TextView placeCallTv = view.findViewById(R.id.places_detail_int_phone_detail);
        TextView placeCompoundTv = view.findViewById(R.id.places_detail_compound_detail);
        TextView placeWebsiteTv = view.findViewById(R.id.places_detail_website_detail);
        TextView placeTimingsTv = view.findViewById(R.id.places_detail_timings);

        placeAddressTv.setText(mDataPlaceAddress);
        placeCallTv.setText(mDataContactNumber);
        if (!mDataContactNumber.contains("Not available")) {
            Linkify.addLinks(placeCallTv, Linkify.PHONE_NUMBERS);
        }
        placeCompoundTv.setText(mDataCompoundAddress);
        placeWebsiteTv.setText(mDataWebsiteUrl);
        if (!mDataWebsiteUrl.contains("Not available")) {
            Linkify.addLinks(placeWebsiteTv, Linkify.WEB_URLS);
        }
        if (mDataIsOpen) {
            placeTimingsTv.setTextColor(Color.parseColor("#2E7D32"));
            placeTimingsTv.setText("Currently Open");
        } else {
            placeTimingsTv.setTextColor(Color.parseColor("#D50000"));
            placeTimingsTv.setText("Currently Closed");
        }

        placeWebsiteTv.setOnClickListener(v -> {
            if (!mDataWebsiteUrl.contains("Not available")) {
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mDataWebsiteUrl));
                urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (urlIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    getActivity().startActivity(urlIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Failed to redirect you to " + mDataWebsiteUrl,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setting calling enabled to phone numbers
        buttonCall.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(placeCallTv.getText().toString())
                    && !placeCallTv.getText().toString().contains("Not available")) {
                openCallingDialog(placeCallTv.getText().toString(), mDataPlaceName);
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Failed to make a call. Number is not available",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Setting navigation to particular destination
        buttonNavigate.setOnClickListener(v -> startNavigation(MapUtil.getInstance()
                .getDestinationBounds()));

        buttonShare.setOnClickListener(v -> openSharePlace());
    }

    private void openCallingDialog(String phoneNumber, String name) {
        Intent intent = new Intent(getActivity(),
                ActivityCall.class);
        intent.putExtra("personName", name);
        intent.putExtra("personContactNumber", phoneNumber.trim());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openSharePlace() {
         NetworkTask.getInstance(0).sharePlaceTask(MapUtil.getInstance()
                        .getDestinationBounds().latitude,
                MapUtil.getInstance().getDestinationBounds().longitude, mStaticMapsReceiver);
    }

    private void sharePlace(String imagePath) {
        if (imagePath != null) {
            Uri imageUri = Uri.parse(imagePath);
            if (imageUri != null) {
                Intent locationShareIntent = new Intent(Intent.ACTION_SEND);
                locationShareIntent.putExtra(Intent.EXTRA_TEXT, mDataPlaceName);
                locationShareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                locationShareIntent.setType("image/*");
                locationShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (locationShareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(locationShareIntent, "Share image via"),
                            REQUEST_SHARE);
                } else {
                    ErrorFragment errorFragment = new ErrorFragment();
                    Bundle msg = new Bundle();
                    msg.putString("msg", "Unable to share your location. No such application found to share.");
                    errorFragment.setArguments(msg);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.message, errorFragment).commit();
                }
            } else {
                ErrorFragment errorFragment = new ErrorFragment();
                Bundle msg = new Bundle();
                msg.putString("msg", "Unable to share your location. No such application found to share.");
                errorFragment.setArguments(msg);
                getActivity().getSupportFragmentManager()
                        .beginTransaction().replace(R.id.message, errorFragment).commit();
            }

        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Failed to share your location", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNavigation(LatLng dest) {
        //The intent chooser displays all the installed apps.
        String suffixCoordinatesLink = dest.latitude + "," + dest.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //build the link url
        StringBuilder link = new StringBuilder();
        link.append(GlobalSettings.PREFIX_DEFAULT_GOOGLE_NAVIGATE + suffixCoordinatesLink);
        intent.setData(Uri.parse(link.toString()));

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                anfe.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Could not start the activity", Toast.LENGTH_SHORT).show();
        }
    }

    private final IStaticMapsListener mStaticMapsReceiver = mapImage -> {
        String imagePath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                mapImage, mDataPlaceName, mDataPlaceAddress);
        if (imagePath != null) {
            sharePlace(imagePath);
        } else {
            Toast.makeText(getActivity(), "Failed to share the place", Toast.LENGTH_SHORT).show();
        }
    };
}