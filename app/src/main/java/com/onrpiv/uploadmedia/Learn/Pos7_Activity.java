package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos7_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos7);

        TextView t0 = (TextView)findViewById(R.id.pos7TextView0);
        t0.setText("Shear");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos7TextView1);
        t1.setText("Shear stress is the stress describing the stress components parallel to the plane of interest in a material cross section. An example of said cross section is shown in the picture below:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos7TextView2);
        t2.setText("The stress tensor describing the figure above is:");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.pos7TextView3);
        t3.setText("Where shear stresses are indicated by \uD835\uDF0F\uD835\uDC56\uD835\uDC57. When examining a Newtonian, laminar fluid flow, the shear stress parallel to a flat plate is described through:");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
