package com.nearby.whatsnearby.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nearby.whatsnearby.R;

/**
 * Created by rudhraksh.pahade on 12-07-2016.
 */

public class ActivityCall extends Activity implements View.OnClickListener {
    private RelativeLayout mRelativeLayout;
    private FloatingActionButton fabCall;
    private FloatingActionButton fabCancelCall;
    private TextView mTextViewCallMsg;
    private TextView mTextViewContactNumber;
    private Animation mAnimation;

    private String personName;
    private String personContactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpWindowParameters();
        super.onCreate(savedInstanceState);
    }

    private void setUpWindowParameters() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_make_call);
        personName = getIntent().getExtras().getString("personName");
        personContactNumber = getIntent().getExtras().getString("personContactNumber");
        mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.error_x_in);
        initViews();
    }

    private void initViews() {
        mRelativeLayout = findViewById(R.id.relativeLayoutMain);
        mRelativeLayout.setAnimation(mAnimation);
        mTextViewCallMsg = findViewById(R.id.callerName);
        mTextViewCallMsg.setText(getResources().getString(R.string.make_call_msg, personName));
        mTextViewContactNumber = findViewById(R.id.callerPhoneNumber);
        mTextViewContactNumber.setText(getResources().getString(R.string.make_call_msg, personContactNumber));
        fabCall = findViewById(R.id.fabCall);
        fabCancelCall = findViewById(R.id.fabCancelCall);
        fabCall.setOnClickListener(this);
        fabCancelCall.setOnClickListener(this);
    }

    private void putCall() {
        try {
            String uri = "tel:" + personContactNumber;
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ActivityCompat.checkSelfPermission(ActivityCall.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            ActivityCall.this.startActivity(callIntent);
            ActivityCall.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ActivityCall.this, "Your call has failed...",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == fabCall) {
            putCall();
        } else if (v == fabCancelCall) {
            ActivityCall.this.finish();
        }
    }
}