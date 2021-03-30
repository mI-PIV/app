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

import com.onrpiv.uploadmedia.Experiment.HomeActivity;
import com.onrpiv.uploadmedia.Experiment.VideoActivity;
import com.onrpiv.uploadmedia.R;

public class LearnFluids extends AppCompatActivity {

    private Button fluidGlossaryButton;
    private Button tryExperimentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.learn_fluids);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fluidGlossaryButton = (Button)findViewById(R.id.fluidGlossaryButton);
        fluidGlossaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIPIVOpen = new Intent(LearnFluids.this, FluidGlossary.class);
                startActivity(mIPIVOpen);
            }
        });

        tryExperimentButton = (Button)findViewById(R.id.tryExperimentButton);
        tryExperimentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tryExperiment = new Intent(LearnFluids.this, TryExperiments.class);
                startActivity(tryExperiment);
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.nav_startExperiment:
                    Intent intent1 = new Intent(LearnFluids.this, VideoActivity.class);
                    startActivity(intent1);
                    break;

                case R.id.nav_Home:
                    System.out.println("Home it is");
                    Intent intent2 = new Intent(LearnFluids.this, HomeActivity.class);
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
