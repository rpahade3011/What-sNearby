package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.permissions.PermissionsPreferences;

public class SplashActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SplashActivity";

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 4000;
    Animation anim;
    ImageView img;
    ImageView img1;
    ImageView img2;
    ImageView img3;
    ImageView img4;
    ImageView img5;
    ImageView img6;
    ImageView img7;
    TextView nearbyTxt;

    private PermissionsPreferences permissionsPreferences = new PermissionsPreferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Setting navigation bar color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));

        img = findViewById(R.id.imageView);
        img1 = findViewById(R.id.imageView2);
        img2 = findViewById(R.id.imageView3);
        img3 = findViewById(R.id.imageView4);
        img4 = findViewById(R.id.imageView5);
        img5 = findViewById(R.id.imageView6);
        img6 = findViewById(R.id.imageView7);
        img7 = findViewById(R.id.imageView8);
        nearbyTxt = findViewById(R.id.nearbyTxt);

        anim = AnimationUtils.loadAnimation(this, R.anim.anim);
        try {
            new Handler().postDelayed((this::startAppNormally), SPLASH_TIME_OUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            img.startAnimation(anim);
            img2.startAnimation(anim);
            img3.startAnimation(anim);
            img4.startAnimation(anim);
            img5.startAnimation(anim);
            img6.startAnimation(anim);
            img7.startAnimation(anim);
            img1.startAnimation(anim);
        }
    }

    private void startAppNormally() {
        if (permissionsPreferences.getApplicationOk(getApplicationContext())) {
            Intent mainInt = new Intent(SplashActivity.this, NavigationController.class);
            SplashActivity.this.startActivity(mainInt);
            SplashActivity.this.finish();
        } else {
            Intent mainIntent = new Intent(SplashActivity.this, PermissionActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }
    }
}