package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos12_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos12);

        TextView t0 = (TextView)findViewById(R.id.pos12TextView0);
        t0.setText("External/Internal Flow");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos12TextView1);
        t1.setText("External and internal flows are a way to describe the fluid flow relative to an object of interest. If the fluid is flowing inside of the object of interest such as in a pipe, the flow is described as internal (below). Internal flow typically has symmetric velocity profiles:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos12TextView2);
        t2.setText("If the fluid is flowing outside of the object of interest such as flow over a cylinder (below), the flow is referred to as external:");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
