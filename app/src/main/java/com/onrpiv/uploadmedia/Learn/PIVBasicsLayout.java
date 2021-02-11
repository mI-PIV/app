package com.onrpiv.uploadmedia.Learn;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.onrpiv.uploadmedia.R;

public class PIVBasicsLayout extends LearnPIV {

    String[] learnPIVArray = {"Learn About PIV", "Parameters1", "Parameters2", "Parameters3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.learn_pivbasics_layout);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, learnPIVArray);

        ListView listView = (ListView)findViewById(R.id.learnpiv_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(view.getContext(), PIVBasics1.class);
                    startActivityForResult(myIntent,0);
                }
                if( position == 1) {
                    Intent myIntent = new Intent(view.getContext(), PIVBasics2.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 2) {
                    Intent myIntent = new Intent(view.getContext(), PIVBasics3.class);
                    startActivityForResult(myIntent,0);
                }
                if (position == 3) {
                    Intent myIntent = new Intent(view.getContext(), PIVBasics4.class);
                    startActivityForResult(myIntent,0);
                }
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
