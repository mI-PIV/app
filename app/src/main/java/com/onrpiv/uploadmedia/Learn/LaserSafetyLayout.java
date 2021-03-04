package com.onrpiv.uploadmedia.Learn;

import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class LaserSafetyLayout extends LearnPIV {
    String [] laserList = {"Laser Definition", "Lasers in mi-PIV", "Safe use of lasers in Classroom"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.laser_safety_layout);

        TextView t1 = (TextView)findViewById(R.id.laserSafetytextview1);
        t1.setText("Do NOT look at the laser beam!");

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, laserList);

        ListView listView = (ListView) findViewById(R.id.laserSafety_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(view.getContext(), Laser1.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 1) {
                    Intent myIntent = new Intent(view.getContext(), Laser2.class);
                    startActivityForResult(myIntent,1);
                }
                if (position == 2) {
                    Intent myIntent = new Intent(view.getContext(), Laser3.class);
                    startActivityForResult(myIntent,2);
                }
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
