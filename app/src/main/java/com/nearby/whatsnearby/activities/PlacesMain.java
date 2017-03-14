package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.fragments.PlacesGrid;

/**
 * Created by rudhraksh.pahade on 11-07-2016.
 */

public class PlacesMain extends FragmentActivity {

    Fragment fragAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_main);

        // Setting navigation bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        ImageView search = (ImageView)findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesMain.this, ActivitySearch.class);
                startActivity(intent);
            }
        });

        fragAll = new PlacesGrid();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.places_grid, fragAll);
        ft.commit();
    }
}
