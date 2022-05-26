package com.onrpiv.uploadmedia.Learn;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.onrpiv.uploadmedia.Experiment.Account;
import com.onrpiv.uploadmedia.Experiment.HomeActivity;
import com.onrpiv.uploadmedia.Experiment.VideoActivity;
import com.onrpiv.uploadmedia.R;

public class LearnPIV extends HomeActivity {

    private Button pivBasicsButton;
    private Button learnAboutImaging;
    private Button videoTutorialButton;
    private Button laserSafetyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.learn_piv);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pivBasicsButton = (Button)findViewById(R.id.pivBasicsButton);
        pivBasicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pivBasics = new Intent(LearnPIV.this, PIVBasicsLayout.class);
                startActivity(pivBasics);
            }
        });

        learnAboutImaging = (Button)findViewById(R.id.learnAboutImagingButton);
        learnAboutImaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent learnImaging = new Intent(LearnPIV.this, LearnImagingLayout.class);
                startActivity(learnImaging);
            }
        });

        videoTutorialButton = (Button)findViewById(R.id.videoTutorialButton);
        videoTutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoTutorial = new Intent(LearnPIV.this, VideoTutorial.class);
                startActivity(videoTutorial);
            }
        });

        laserSafetyButton = (Button)findViewById(R.id.laserSafetyButton);
        laserSafetyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent laserSafety = new Intent(LearnPIV.this, LaserSafetyLayout.class);
                startActivity(laserSafety);
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
