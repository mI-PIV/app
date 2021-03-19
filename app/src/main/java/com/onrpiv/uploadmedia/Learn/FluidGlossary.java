package com.onrpiv.uploadmedia.Learn;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.onrpiv.uploadmedia.Experiment.HomeActivity;
import com.onrpiv.uploadmedia.Experiment.VideoActivity;
import com.onrpiv.uploadmedia.R;

public class FluidGlossary extends LearnFluids {
    // Array of strings...
    String[] mobileArray = {"Boundary Layer","Laminar and Turbulent Flow","Reynolds Number","Vorticity/Circulation",
            "Fluid","Wake","Shear","Velocity profile","Streamline","Steady/Unsteady",
            "Bernoulli Equation","External/Internal Flow","Major/Minor losses","Common Assumptions","Viscosity"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fluid_glossary);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, mobileArray);

        final ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (position == 0) {
                    Intent myIntent = new Intent(view.getContext(), Pos1_Activity.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 1) {
                    Intent myIntent = new Intent(view.getContext(), Pos2_Activity.class);
                    startActivityForResult(myIntent,1);
                }
                if (position == 2) {
                    Intent myIntent = new Intent(view.getContext(), Pos3_Activity.class);
                    startActivityForResult(myIntent,2);
                }
                if (position == 3) {
                    Intent myIntent = new Intent(view.getContext(), Pos4_Activity.class);
                    startActivityForResult(myIntent,3);
                }
                if (position == 4) {
                    Intent myIntent = new Intent(view.getContext(), Pos5_Activity.class);
                    startActivityForResult(myIntent,4);
                }
                if (position == 5) {
                    Intent myIntent = new Intent(view.getContext(), Pos6_Activity.class);
                    startActivityForResult(myIntent,5);
                }
                if (position == 6) {
                    Intent myIntent = new Intent(view.getContext(), Pos7_Activity.class);
                    startActivityForResult(myIntent,6);
                }
                if (position == 7) {
                    Intent myIntent = new Intent(view.getContext(), Pos8_Activity.class);
                    startActivityForResult(myIntent,7);
                }
                if (position == 8) {
                    Intent myIntent = new Intent(view.getContext(), Pos9_Activity.class);
                    startActivityForResult(myIntent,8);
                }
                if (position == 9) {
                    Intent myIntent = new Intent(view.getContext(), Pos10_Activity.class);
                    startActivityForResult(myIntent,9);
                }
                if (position == 10) {
                    Intent myIntent = new Intent(view.getContext(), Pos11_Activity.class);
                    startActivityForResult(myIntent,10);
                }
                if (position == 11) {
                    Intent myIntent = new Intent(view.getContext(), Pos12_Activity.class);
                    startActivityForResult(myIntent,11);
                }
                if (position == 12) {
                    Intent myIntent = new Intent(view.getContext(), Pos13_Activity.class);
                    startActivityForResult(myIntent,12);
                }
                if (position == 13) {
                    Intent myIntent = new Intent(view.getContext(), Pos14_Activity.class);
                    startActivityForResult(myIntent,13);
                }
                // Commenting this out because there's no content in this section yet.
                if (position == 14) {
//                    Intent myIntent = new Intent(view.getContext(), Pos15_Activity.class);
//                    startActivityForResult(myIntent,14);
                    //view.setBackgroundColor(Color.parseColor("#bebebe"));
                }
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
                    Intent intent1 = new Intent(FluidGlossary.this, VideoActivity.class);
                    startActivity(intent1);
                    break;

                case R.id.nav_Home:
                    System.out.println("Home it is");
                    Intent intent2 = new Intent(FluidGlossary.this, HomeActivity.class);
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