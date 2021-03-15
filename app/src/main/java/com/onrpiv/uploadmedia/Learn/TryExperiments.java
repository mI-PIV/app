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

                // There's only one experiment that's completed in this section. Graying out the ones
                // that don't have any content.
//                if (position == 0) {
//                    view.setBackgroundColor(Color.parseColor("#bebebe"));
//                }
//                if (position == 1) {
//                    view.setBackgroundColor(Color.parseColor("#bebebe"));
//                }
//                if (position == 2) {
//                    view.setBackgroundColor(Color.parseColor("#bebebe"));
//                }
//                if (position == 3) {
//                    view.setBackgroundColor(Color.parseColor("#bebebe"));
//                }
//                if (position == 4) {
//                    view.setBackgroundColor(Color.parseColor("#bebebe"));
//                }
                if (position == 5) {
                    Intent myIntent = new Intent(view.getContext(), Experiment1.class);
                    startActivityForResult(myIntent,0);
                }
//                if (position == 6) {
//                    view.setBackgroundColor(Color.parseColor("#bebebe"));
//                }
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

    }
}
