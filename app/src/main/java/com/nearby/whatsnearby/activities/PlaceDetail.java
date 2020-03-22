package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.nearby.whatsnearby.AlertType;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.beans.PlaceDetailParser;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.fragments.error.ErrorFragment;
import com.nearby.whatsnearby.requests.NetworkTask;
import com.nearby.whatsnearby.utilities.Utils;

public class PlaceDetail extends FragmentActivity implements FetchFromServerUser {

    private Fragment errorFragment;
    private SweetAlertDialog sweetAlertDialog;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_detail);

        // Setting navigation bar color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> PlaceDetail.this.finish());

        ImageView search = findViewById(R.id.search);
        search.setOnClickListener(v -> {
            Intent intent = new Intent(PlaceDetail.this, ActivitySearch.class);
            startActivity(intent);
        });

        Intent intent = getIntent();
        String placeId = intent.getStringExtra("placeId");
        url = Utils.getInstance().getSearchedPlaceDetailsUrl(placeId);
        Log.e("PlaceDetail", url);
        NetworkTask.getInstance(this, 0).executeSearchedPlaceDetailTask(url);
    }

    @Override
    public void onPreFetch(AlertType alertType) {
        sweetAlertDialog = new SweetAlertDialog(PlaceDetail.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Retrieving information");
        sweetAlertDialog.setContentText("Please be patient");
        sweetAlertDialog.show();
    }

    @Override
    public void onFetchCompletion(String string, int id, AlertType alertType) {
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
        NetworkTask.getInstance(this, 0).executeSearchedPlaceDetailTask(url);
    }
}