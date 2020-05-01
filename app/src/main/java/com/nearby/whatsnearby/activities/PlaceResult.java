package com.nearby.whatsnearby.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nearby.whatsnearby.AlertType;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.PlaceListAdapter;
import com.nearby.whatsnearby.adapters.RecyclerItemClickListener;
import com.nearby.whatsnearby.beans.PlaceBean;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.beans.PlaceDetailParser;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.fragments.error.ErrorFragment;
import com.nearby.whatsnearby.places.JSONParser;
import com.nearby.whatsnearby.requests.NetworkTask;
import com.nearby.whatsnearby.services.GpsTracker;
import com.nearby.whatsnearby.utilities.Utils;

import java.util.List;

public class PlaceResult extends FragmentActivity implements FetchFromServerUser {

    private ErrorFragment errorFragment;
    private Context context = this;
    private RecyclerView listOfPlaces;
    private SweetAlertDialog sweetAlertDialog;
    private GpsTracker loc;
    private String kind;
    private String url;

    private double locLat = 0;
    private double locLng = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_list);

        // Setting navigation bar color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));

        loc = new GpsTracker(this);
        if (loc.canGetLocation()) {
            locLat = loc.getLatitude();
            locLng = loc.getLongitude();
        } else {
            Log.e("PlaceResult", "Unable to get your location");
        }

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> onBackPressed());

        ImageView search = findViewById(R.id.search);
        search.setOnClickListener(v -> {
            Intent intent = new Intent(PlaceResult.this, ActivitySearch.class);
            startActivity(intent);
        });

        kind = getIntent().getStringExtra("Place_id");
        TextView placeKind = findViewById(R.id.namePlaceHolder);
        placeKind.setText(kind.replace("_", " "));

        if (locLat != 0 && locLng != 0) {
            url = Utils.getInstance().getNearbySearchUrl(kind, locLat, locLng);
        } else {
            if (loc.canGetLocation()) {
                locLat = loc.getLatitude();
                locLng = loc.getLongitude();

                if (locLat != 0 && locLng != 0) {
                    url = Utils.getInstance().getNearbySearchUrl(kind, locLat, locLng);
                }
            } else {
                Log.e("PlaceResult", "Unable to get your location");
            }
        }
        Log.i("PlaceResult", "PlaceResult" + url);
        NetworkTask.getInstance(0).executeNearbyPlacesTask(this, url);
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }

    @Override
    public void onPreFetch(AlertType alertType) {
        switch (alertType) {
            case DISCOVER_NEARBY_PLACES:
                sweetAlertDialog = new SweetAlertDialog(PlaceResult.this,
                        SweetAlertDialog.PROGRESS_TYPE);
                sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                sweetAlertDialog.setTitleText("Looking at your nearby");
                sweetAlertDialog.setContentText("Please be patient");
                sweetAlertDialog.show();
                break;
            case GET_PLACE_DETAILS:
                if (sweetAlertDialog == null) {
                    sweetAlertDialog = new SweetAlertDialog(PlaceResult.this,
                            SweetAlertDialog.PROGRESS_TYPE);
                    sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    sweetAlertDialog.setTitleText("Retrieving information");
                    sweetAlertDialog.setContentText("Please be patient");
                    sweetAlertDialog.show();
                }
                break;
        }
    }

    @Override
    public void onFetchCompletion(String string, int id, AlertType alertType) {
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismissWithAnimation();
            sweetAlertDialog = null;
        }
        if (errorFragment != null)
            getSupportFragmentManager().beginTransaction().remove(errorFragment).commit();
        if (string == null || string.equals("")) {
            errorFragment = new ErrorFragment();
            Bundle msg = new Bundle();
            msg.putString("msg", "No or poor internet connection.");
            errorFragment.setArguments(msg);
            getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment).commit();
        } else {
            parseResponse(string, alertType);
        }
    }

    private void parseResponse(String response, AlertType alertType) {
        switch (alertType) {
            case DISCOVER_NEARBY_PLACES:
                JSONParser parser = new JSONParser(response, kind);
                try {
                    final List<PlaceBean> list = parser.getPlaceBeanList();
                    if (list != null && list.size() > 0) {
                        PlaceListAdapter placesAdapter = new PlaceListAdapter(context, list, loc);
                        listOfPlaces = findViewById(R.id.list);
                        listOfPlaces.setHasFixedSize(true);
                        listOfPlaces.setLayoutManager(new LinearLayoutManager(context));
                        listOfPlaces.setAdapter(placesAdapter);

                        listOfPlaces.addOnItemTouchListener(new RecyclerItemClickListener(context,
                                (view, position) -> getPlaceDetails(list.get(position).getPlaceref())));
                    } else {
                        LinearLayout lLayoutBackground = findViewById(R.id.background);
                        lLayoutBackground.setVisibility(View.GONE);
                        TextView no_places = findViewById(R.id.no_places);
                        no_places.setVisibility(View.VISIBLE);
                        no_places.setText("Sorry, no such place found in your nearby.");
                    }
                } catch (Exception ex) {
                    errorFragment = new ErrorFragment();
                    Bundle msg = new Bundle();
                    msg.putString("msg", ex.getMessage());
                    errorFragment.setArguments(msg);
                    getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment)
                            .commit();
                }
                break;
            case GET_PLACE_DETAILS:
                try {
                    PlaceDetailParser jsonParser = new PlaceDetailParser(response);
                    final PlaceDetailBean detailBean = jsonParser.getPlaceDetail();

                    Intent intent = new Intent(PlaceResult.this,
                            DirectionsActivity.class);
                    Bundle data = new Bundle();
                    data.putDouble("Lat", detailBean.getLat());
                    data.putDouble("Lng", detailBean.getLng());
                    data.putString("Name", detailBean.getName());
                    data.putString("Address", detailBean.getFormatted_address());
                    data.putBoolean("Timing", detailBean.isOpen());
                    data.putString("Place_Category", kind.replace("_", " "));
                    data.putString("CompoundAddress", detailBean.getCompoundAddress());
                    data.putString("ContactNumber", detailBean.getInternational_phone_number());
                    data.putFloat("PlaceRatings", detailBean.getRating());
                    data.putString("Place_Website", detailBean.getWebsiteUrl());
                    intent.putExtras(data);
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorFragment = new ErrorFragment();
                    Bundle msg = new Bundle();
                    msg.putString("msg", ex.getMessage());
                    errorFragment.setArguments(msg);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.message, errorFragment).commit();
                }
                break;
        }
    }

    private void getPlaceDetails(String placeRef) {
        NetworkTask.getInstance(1).getPlaceDetails(this, placeRef);
    }

    public void retry(View view) {
        NetworkTask.getInstance(0).executeNearbyPlacesTask(this, url);
    }
}