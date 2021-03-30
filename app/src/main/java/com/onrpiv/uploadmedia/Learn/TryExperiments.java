package com.onrpiv.uploadmedia.Learn;

import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.onrpiv.uploadmedia.R;

public class TryExperiments extends FluidGlossary {

    String[] experimentList = {"Bubble Curtain","Deep Sea Vent","Flow Over Propeller Hulls","Fully Developed Pipe Flow",
            "Laminar Transition Pipe Flow","Pipe Flow Vortex Generator","Seed Particle Size Effects"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.try_experiments);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, experimentList);

        ListView listView = (ListView) findViewById(R.id.experimentList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // There's only one experiment that's completed in this section.
                // The rest are "under construction."
                if (position == 0) {
                    Intent myIntent = new Intent(view.getContext(), Experiment1.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 1) {
                    Intent myIntent = new Intent(view.getContext(), Experiment2.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 2) {
                    Intent myIntent = new Intent(view.getContext(), Experiment3.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 3) {
                    Intent myIntent = new Intent(view.getContext(), Experiment4.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 4) {
                    Intent myIntent = new Intent(view.getContext(), Experiment5.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 5) {
                    Intent myIntent = new Intent(view.getContext(), Experiment6.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 6) {
                    Intent myIntent = new Intent(view.getContext(), Experiment7.class);
                    startActivityForResult(myIntent,0);
                }
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

    }
}
