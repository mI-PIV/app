package com.onrpiv.uploadmedia.Learn;

import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.onrpiv.uploadmedia.R;

public class LearnImagingLayout extends LearnPIV {

    String[] imagingArray = {"How Does a Digital Video Camera Work?","Bit Depth","Pixel","ISO",
            "Shutter Speed","Resolution","Focus","Frame Rate","Aperture"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.learn_imaging_layout);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, imagingArray);

        ListView listView = (ListView) findViewById(R.id.imaging_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(view.getContext(), Imaging1.class);
                    startActivityForResult(myIntent,0);
                }
                if( position == 1) {
                    Intent myIntent = new Intent(view.getContext(), Imaging2.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 2) {
                    Intent myIntent = new Intent(view.getContext(), Imaging3.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 3) {
                    Intent myIntent = new Intent(view.getContext(), Imaging4.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 4) {
                    Intent myIntent = new Intent(view.getContext(), Imaging5.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 5) {
                    Intent myIntent = new Intent(view.getContext(), Imaging6.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 6) {
                    Intent myIntent = new Intent(view.getContext(), Imaging7.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 7) {
                    Intent myIntent = new Intent(view.getContext(), Imaging8.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 8) {
                    Intent myIntent = new Intent(view.getContext(), Imaging9.class);
                    startActivityForResult(myIntent,0);
                }
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
