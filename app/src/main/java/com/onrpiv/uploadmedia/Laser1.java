package com.onrpiv.uploadmedia;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

public class Laser1 extends LaserSafetyDummy {
    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser1);
        TextView t0 = (TextView)findViewById(R.id.laserSafetyTextView0);
        t0.setText("Laser Definition");
        t0.setTextSize(headerTextSize);
        TextView t1 = (TextView)findViewById(R.id.laserSafetyTextView1);
        String html1 = "Lasers differ from other types of light sources because they are coherent, meaning they have the same wavelength, phase, and frequency. A laser emits light using optical amplification from stimulated emission of electromagnetic radiation.";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        t1.setTextSize(paraTextSize);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
