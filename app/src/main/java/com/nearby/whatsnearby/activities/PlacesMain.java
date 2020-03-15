package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.fragments.PlacesGrid;

/**
 * Created by rudhraksh.pahade on 11-07-2016.
 */

public class PlacesMain extends FragmentActivity {

    private Fragment fragAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_main);

        // Setting navigation bar color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));

        ImageView search = findViewById(R.id.search);
        search.setOnClickListener(v -> {
            Intent intent = new Intent(PlacesMain.this, ActivitySearch.class);
            startActivity(intent);
        });

        fragAll = new PlacesGrid();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.places_grid, fragAll);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }
}