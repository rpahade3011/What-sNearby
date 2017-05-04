package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.beans.PlaceDetailParser;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.customasynctask.FetchFromServerTask;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.fragments.ErrorFragment;
import com.nearby.whatsnearby.services.AppController;

public class PlaceDetail extends FragmentActivity implements FetchFromServerUser {

    Fragment errorFragment;
    private SweetAlertDialog sweetAlertDialog;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_detail);

        // Setting navigation bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaceDetail.this.finish();
            }
        });

        ImageView search = (ImageView) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaceDetail.this, ActivitySearch.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String placeId = intent.getStringExtra("placeId");
        String KEY = AppController.getInstance().getResources().getString(R.string.google_places_search_server_key);
        url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&key=" + KEY;
        Log.e("PlaceDetail", url);
        new FetchFromServerTask(this, 0).execute(url);
    }

    @Override
    public void onPreFetch() {
        sweetAlertDialog = new SweetAlertDialog(PlaceDetail.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Retrieving information");
        sweetAlertDialog.setContentText("Please be patient");
        sweetAlertDialog.show();
    }

    @Override
    public void onFetchCompletion(String string, int id) {
        if (sweetAlertDialog != null)
            sweetAlertDialog.dismissWithAnimation();
        if (errorFragment != null)
            getSupportFragmentManager().beginTransaction().remove(errorFragment).commit();
        if (string == null || string.equals("")) {
            errorFragment = new ErrorFragment();
            Bundle msg = new Bundle();
            msg.putString("msg", "No or poor internet connection.");
            errorFragment.setArguments(msg);
            getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment).commit();
        } else {
            try {
                PlaceDetailParser jsonParser = new PlaceDetailParser(string);
                final PlaceDetailBean detailBean = jsonParser.getPlaceDetail();

                Intent intent = new Intent(PlaceDetail.this, AboutPlaceDetailActivity.class);
                Bundle data = new Bundle();
                data.putDouble("Lat", detailBean.getLat());
                data.putDouble("Lng", detailBean.getLng());
                data.putString("Name", detailBean.getName());
                data.putString("Address", detailBean.getFormatted_address());
                data.putString("ContactNumber", detailBean.getInternational_phone_number());
                data.putFloat("PlaceRatings", detailBean.getRating());
                data.putStringArray("photos", detailBean.getPhotos());
                intent.putExtras(data);
                startActivity(intent);

                PlaceDetail.this.finish();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorFragment = new ErrorFragment();
                Bundle msg = new Bundle();
                msg.putString("msg", ex.getMessage());
                errorFragment.setArguments(msg);
                getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment).commit();
            }
        }
    }

    public void retry(View view) {
        new FetchFromServerTask(this, 0).execute(url);
    }

}
