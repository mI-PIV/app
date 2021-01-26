package com.onrpiv.uploadmedia;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {
    private Button startExperimentButton;
    private Button learnAboutFluidsButton;
    private Button learnPIVButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        startExperimentButton = (Button)findViewById(R.id.startExperimentButton);
        startExperimentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIPIVOpen = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(mIPIVOpen);
            }
        });
        learnAboutFluidsButton = (Button)findViewById(R.id.learnFluidsButton);
        learnAboutFluidsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fluidsOpen = new Intent(HomeActivity.this, LearnFluids.class);
                startActivity(fluidsOpen);
            }
        });
        learnPIVButton = (Button)findViewById(R.id.learnPIVButton);
        learnPIVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent learnPIV = new Intent(HomeActivity.this, LearnPIV.class);
                startActivity(learnPIV);
            }
        });
    }
}
