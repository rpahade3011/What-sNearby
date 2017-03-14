package com.nearby.whatsnearby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.nearby.whatsnearby.R;
import com.vansuita.materialabout.builder.AboutBuilder;

/**
 * Created by rudhraksh.pahade on 25-07-2016.
 */

public class ProfileActivity extends AppCompatActivity {
    private static final int theme = R.style.AppThemeLight;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTheme(theme);
        loadAboutMe();
    }

    private void loadAboutMe() {
        final FrameLayout flHolder = (FrameLayout) findViewById(R.id.aboutme);

        if (flHolder != null) {
            flHolder.addView(
                    AboutBuilder.with(this)
                            .setAppIcon(R.mipmap.ic_launcher)
                            .setAppName(R.string.app_name)
                            .setAppTitle(R.string.app_desc)
                            .setPhoto(R.mipmap.about_rudraksh_pahade_profile_pic)
                            .setCover(R.drawable.wn_mainbg)
                            .setLinksAnimated(false)
                            .setDividerDashGap(13)
                            .setName("Rudraksh Pahade")
                            .setNameColor(R.color.colorPrimary)
                            .setSubTitle("Mobile Developer")
                            .setLinksColumnsCount(3)
                            .setBrief("I'm warmed of mobile technologies. Ideas maker, curious and nature lover.")
                            .addGooglePlayStoreLink("109312616470328191163")
                            .addGitHubLink("rpahade3011")
                            .addFacebookLink("rudraksh.pahade")
                            .addTwitterLink("pahade_rudraksh")
                            .addInstagramLink("rudrakshpahade")
                            .addGooglePlusLink("109312616470328191163")
                            .addLinkedInLink("rudraksh-pahade-752b3b3a")
                            .addEmailLink("rudraksh3011@gmail.com")
                            .addWhatsappLink("Rudraksh", "+919028411974")
                            .addSkypeLink("rudraksh.pahade")
                            .addGoogleLink("rudraksh3011")
                            .addFiveStarsAction()
                            .addMoreFromMeAction("Rudraksh+Pahade")
                            .setVersionNameAsAppSubTitle()
                            .addShareAction(R.string.app_name)
                            .addUpdateAction()
                            .setActionsColumnsCount(2)
                            .addFeedbackAction("rudraksh3011@gmail.com")
                            .addIntroduceAction((Intent) null)
                            .addHelpAction((Intent) null)
                            .addChangeLogAction((Intent) null)
                            .build());
        }
    }
}
