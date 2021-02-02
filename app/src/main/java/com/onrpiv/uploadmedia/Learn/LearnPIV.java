package com.onrpiv.uploadmedia.Learn;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.onrpiv.uploadmedia.Experiment.HomeActivity;
import com.onrpiv.uploadmedia.Experiment.VideoActivity;
import com.onrpiv.uploadmedia.R;

public class LearnPIV extends AppCompatActivity {

    private Button pivBasicsButton;
    private Button learnAboutImaging;
    private Button videoTutorialButton;
    private Button laserSafetyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.learn_piv);

        pivBasicsButton = (Button)findViewById(R.id.pivBasicsButton);
        pivBasicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pivBasics = new Intent(LearnPIV.this, PIVBasics.class);
                startActivity(pivBasics);
            }
        });

        learnAboutImaging = (Button)findViewById(R.id.learnAboutImagingButton);
        learnAboutImaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent learnImaging = new Intent(LearnPIV.this, LearnImagingDummy.class);
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
                Intent laserSafety = new Intent(LearnPIV.this, LaserSafetyDummy.class);
                startActivity(laserSafety);
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    public BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.nav_startExperiment:
                    Intent intent1 = new Intent(LearnPIV.this, VideoActivity.class);
                    startActivity(intent1);
                    break;

                case R.id.nav_Home:
                    System.out.println("Home it is");
                    Intent intent2 = new Intent(LearnPIV.this, HomeActivity.class);
                    startActivity(intent2);
                    break;

                case R.id.nav_feedback:
                    System.out.println("Feedback it is");
                    Intent intent3 = new Intent("android.intent.action.VIEW", Uri.parse("https://usu.co1.qualtrics.com/jfe/form/SV_3WtfQHquWuN0ujj"));
                    startActivity(intent3);
                    break;
            }
            return true;
        }
    };
}
