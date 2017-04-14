package com.nearby.whatsnearby.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.PlaceListAdapter;
import com.nearby.whatsnearby.adapters.RecyclerItemClickListener;
import com.nearby.whatsnearby.beans.PlaceBean;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.beans.PlaceDetailParser;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.customasynctask.FetchFromServerTask;
import com.nearby.whatsnearby.customasynctask.FetchFromServerUser;
import com.nearby.whatsnearby.customasynctask.Streams;
import com.nearby.whatsnearby.fragments.ErrorFragment;
import com.nearby.whatsnearby.places.JSONParser;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.services.GpsTracker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PlaceResult extends FragmentActivity implements FetchFromServerUser {

    private static final String KEY = "AIzaSyA2nMz4vfd-wyeivmvJffVB5RP59POoTm0";
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        loc = new GpsTracker(this);
        if (loc.canGetLocation()) {
            locLat = loc.getLatitude();
            locLng = loc.getLongitude();
        } else {
            Log.e("PlaceResult", "Unable to get your location");
        }

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView search = (ImageView) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaceResult.this, ActivitySearch.class);
                startActivity(intent);
            }
        });

        kind = getIntent().getStringExtra("Place_id");
        TextView placeKind = (TextView) findViewById(R.id.namePlaceHolder);
        placeKind.setText(kind.replace("_", " "));

        if (locLat != 0 && locLng != 0) {
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                    locLat + "," + locLng + "&rankby=distance&types=" + kind + "&key=" + KEY;
        } else {
            if (loc.canGetLocation()) {
                locLat = loc.getLatitude();
                locLng = loc.getLongitude();

                if (locLat != 0 && locLng != 0) {
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                            locLat + "," + locLng + "&rankby=distance&types=" + kind + "&key=" + KEY;
                }
            } else {
                Log.e("PlaceResult", "Unable to get your location");
            }
        }
        System.out.println("PlaceResult" + url);

        new FetchFromServerTask(this, 0).execute(url);
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }

    @Override
    public void onPreFetch() {
        sweetAlertDialog = new SweetAlertDialog(PlaceResult.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Looking at your nearby");
        sweetAlertDialog.setContentText("Please be patient");
        sweetAlertDialog.show();
    }

    @Override
    public void onFetchCompletion(String string, int id) {

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
            JSONParser parser = new JSONParser(string, kind);
            try {
                final List<PlaceBean> list = parser.getPlaceBeanList();
                if (list != null && list.size() > 0) {
                    PlaceListAdapter Places_adapter = new PlaceListAdapter(context, list, loc);
                    listOfPlaces = (RecyclerView) findViewById(R.id.list);
                    listOfPlaces.setHasFixedSize(true);
                    listOfPlaces.setLayoutManager(new LinearLayoutManager(context));
                    listOfPlaces.setAdapter(Places_adapter);

                    listOfPlaces.addOnItemTouchListener(new RecyclerItemClickListener(context,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    getPlaceDetails(list.get(position).getPlaceref());
                                }
                            }));
                } else {
                    LinearLayout lLayoutBackground = (LinearLayout) findViewById(R.id.background);
                    lLayoutBackground.setVisibility(View.GONE);
                    TextView no_places = (TextView) findViewById(R.id.no_places);
                    no_places.setVisibility(View.VISIBLE);
                    no_places.setText("Sorry, no such place found in your nearby.");
                }
            } catch (Exception ex) {
                errorFragment = new ErrorFragment();
                Bundle msg = new Bundle();
                msg.putString("msg", ex.getMessage());
                errorFragment.setArguments(msg);
                getSupportFragmentManager().beginTransaction().replace(R.id.message, errorFragment).commit();
            }
        }
    }

    private void getPlaceDetails(String placeRef) {
        String KEY = AppController.getInstance().getResources().getString(R.string.google_places_search_server_key);
        url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeRef + "&key=" + KEY;
        Log.e("PlaceDetail", url);

        new AsyncTask<String, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (sweetAlertDialog == null) {
                    sweetAlertDialog = new SweetAlertDialog(PlaceResult.this, SweetAlertDialog.PROGRESS_TYPE);
                    sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    sweetAlertDialog.setTitleText("Retrieving information");
                    sweetAlertDialog.setContentText("Please be patient");
                    sweetAlertDialog.show();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                URL urlCould;
                HttpURLConnection connection;
                InputStream inputStream = null;
                try {
                    String url = params[0];
                    urlCould = new URL(url);
                    connection = (HttpURLConnection) urlCould.openConnection();
                    connection.setConnectTimeout(30000);
                    connection.setReadTimeout(30000);
                    connection.setRequestMethod("GET");
                    connection.connect();

                    inputStream = connection.getInputStream();

                } catch (MalformedURLException MEx) {

                } catch (IOException IOEx) {
                    Log.e("Utils", "HTTP failed to fetch data");
                    return null;
                }
                return Streams.readStream(inputStream);
            }

            @Override
            protected void onPostExecute(String string) {
                super.onPostExecute(string);
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

                        Intent intent = new Intent(PlaceResult.this, AboutPlaceDetailActivity.class);
                        Bundle data = new Bundle();
                        data.putDouble("Lat", detailBean.getLat());
                        data.putDouble("Lng", detailBean.getLng());
                        data.putString("Name", detailBean.getName());
                        data.putString("Address", detailBean.getFormatted_address());
                        data.putString("ContactNumber", detailBean.getInternational_phone_number());
                        data.putFloat("PlaceRatings", detailBean.getRating());
                        intent.putExtras(data);
                        startActivity(intent);

                        PlaceResult.this.finish();
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
        }.execute(url);
    }

    public void retry(View view) {
        new FetchFromServerTask(this, 0).execute(url);
    }
}