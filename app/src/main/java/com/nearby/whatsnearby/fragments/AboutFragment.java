package com.nearby.whatsnearby.fragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.PlacesConstants;
import com.nearby.whatsnearby.customasynctask.ExecuteDirectionsAPI;
import com.nearby.whatsnearby.services.GpsTracker;
import com.nearby.whatsnearby.utilities.MapUtil;


public class AboutFragment extends Fragment implements View.OnClickListener {

    public static final String PREFIX_DEFAULT_GOOGLE_NAVIGATE = "google.navigation:q=";
    private GoogleMap mMap;
    private double lat;
    private double lng;
    private String name;
    private String eta;
    private FragmentManager fm;
    private SupportMapFragment fragment;
    private LatLng source;
    private LatLng destination;
    private GpsTracker gpsTracker;
    private FloatingActionButton fabNavigate;
    private TextView textViewTimeEstimationMsg;
    private TextView textViewMinutes;
    private RelativeLayout relativeLayoutEstimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        lat = getArguments().getDouble("Lat");
        lng = getArguments().getDouble("Lng");
        name = getArguments().getString("Name");
        //relativeLayoutEstimation = (RelativeLayout) view.findViewById(R.id.relativeLayoutEstimation);
        //imageViewTimeEstimation = (CrystalPreloader) view.findViewById (R.id.imageViewTimeEstimation);
        textViewTimeEstimationMsg = (TextView) view.findViewById(R.id.tvTimeEstimation);
        //textViewMinutes = (TextView) view.findViewById (R.id.textViewMinutes);
        fabNavigate = (FloatingActionButton) view.findViewById(R.id.fabNavigate);
        fabNavigate.setOnClickListener(this);
        getUsersLocation();
        setUpMapIfNeeded();
        //showDistanceAndTimeEta();
        return view;
    }

    private void getUsersLocation() {
        gpsTracker = new GpsTracker(getActivity());
        source = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        destination = new LatLng(lat, lng);
        MapUtil.sourceBounds = source;
        MapUtil.destinationBounds = destination;
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            fm = getChildFragmentManager();
            fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
            if (fragment == null) {
                fragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.map, fragment).commit();
            }
        }
    }

    private void startNavigation(LatLng dest) {
        //The intent chooser displays all the installed apps.
        String suffixCoordinatesLink = destination.latitude + "," + destination.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //build the link url
        StringBuilder link = new StringBuilder();
        link.append(PREFIX_DEFAULT_GOOGLE_NAVIGATE + suffixCoordinatesLink);
        intent.setData(Uri.parse(link.toString()));

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                anfe.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Could not start the activity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap == null) {
            mMap = fragment.getMap();
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ub_pin_pickup))
                    .position(source).title("My location"));
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ub_pin_destination))
                    .position(/*new LatLng(lat, lng)*/destination).title(name));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12.0f/*15.0f*/));
            drawRoute();
        }
    }

    private void drawRoute() {
        String directionUrl = MapUtil.getDirectionUrl(source, destination);
        Log.d("DirectionURL", directionUrl);
        new ExecuteDirectionsAPI(getActivity(), mMap)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, directionUrl);
    }

    private void showDistanceAndTimeEta() {
        relativeLayoutEstimation.setVisibility(View.VISIBLE);

        // Performing animation on ImageView
        final RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.2f, Animation.RELATIVE_TO_SELF, 0.2f);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setDuration((long) 2 * 1500);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        textViewMinutes.setText(PlacesConstants.distanceEta);
        textViewTimeEstimationMsg.setText(getActivity().getResources().getString(R.string.time_estimation, PlacesConstants.distanceEta));
    }

    @Override
    public void onClick(View v) {
        if (v == fabNavigate) {
            startNavigation(destination);
        }
    }

}
