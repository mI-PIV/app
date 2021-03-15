package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos3_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos3);

        TextView t0 = (TextView)findViewById(R.id.pos3TextView0);
        t0.setText("Reynolds Number");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos3TextView1);
        t1.setText("The Reynolds number (\uD835\uDC45\uD835\uDC52) is used to determine whether a flow is laminar or turbulent. The Reynolds number is the ratio of inertial fluid forces to viscous forces as described in the equation below:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos3TextView2);
        t2.setText("Where \uD835\uDF0C is the fluid density, \uD835\uDC62 is the flow velocity, \uD835\uDC3F\uD835\uDC50 is the characteristic length, and \uD835\uDF07 is the dynamic viscosity of the fluid. The critical Reynolds number is the Reynolds number where the flow begins the transition from laminar to turbulent. For pipe flow, Reynolds numbers below 2300 are typically laminar, and flow with Reynolds numbers above 2900 are fully turbulent. For other flows, a critical Reynolds number of 500,000 is common.");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.pos3TextView3);
        t3.setText("The Reynolds number is also a way to achieve similitude. By matching the Reynolds number, engineers can model flow fields in less expensive ways. For example, water is often used to mimic air with small scaled models, making the lab space and model construction more affordable.");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
