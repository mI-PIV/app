package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos10_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos10);

        TextView t0 = (TextView)findViewById(R.id.pos10TextView0);
        t0.setText("Steady/Unsteady");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos10TextView1);
        t1.setText("When a fluid flow does not change with time, it is referred to as steady. If the flow field changes depending on the time it is observed, it is referred to as unsteady. An example of the differences between steady and unsteady flow is a hose fed by a faucet. Although the flow changes after the faucet is initially opened, after a reasonable amount of time a steady state is reached, and the flow is invariant of time until the faucet is turned on again.");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
