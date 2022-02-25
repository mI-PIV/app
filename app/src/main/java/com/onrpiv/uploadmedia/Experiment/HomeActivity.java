package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.onrpiv.uploadmedia.BuildConfig;
import com.onrpiv.uploadmedia.Learn.LearnFluids;
import com.onrpiv.uploadmedia.Learn.LearnPIV;
import com.onrpiv.uploadmedia.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button startExperimentButton = (Button) findViewById(R.id.startExperimentButton);
        startExperimentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIPIVOpen = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(mIPIVOpen);
            }
        });

        Button learnAboutFluidsButton = (Button) findViewById(R.id.learnFluidsButton);
        learnAboutFluidsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fluidsOpen = new Intent(HomeActivity.this, LearnFluids.class);
                startActivity(fluidsOpen);
            }
        });

        Button learnPIVButton = (Button) findViewById(R.id.learnPIVButton);
        learnPIVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent learnPIV = new Intent(HomeActivity.this, LearnPIV.class);
                startActivity(learnPIV);
            }
        });
    }

    public void about_onclick(View view) {
        final String versionName = BuildConfig.VERSION_NAME;
        new AlertDialog.Builder(HomeActivity.this)
              .setMessage("mI-PIV\nVersion: " + versionName)
              .setPositiveButton("Close", null)
              .setCancelable(true)
              .create().show();
    }

    // ensuring the screen is locked to vertical position
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
