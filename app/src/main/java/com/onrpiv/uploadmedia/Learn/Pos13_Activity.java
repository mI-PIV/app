package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos13_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos13);

        TextView t0 = (TextView)findViewById(R.id.pos13TextView0);
        t0.setText("Major/Minor Losses");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos13TextView1);
        t1.setText("Engineers describe the energy of a flowing fluid in a pipe as head. Major and minor losses refer to the effect of viscosity on decreasing the energy (or head) of a flowing fluid in a pipe. This internal friction is described through major losses (the loss of energy to the fluid contact of the inside of the pipe) and minor losses (the loss of energy due to bends, expansions, and contractions in the pipe). The sum of the major and minor losses is referred to as total head loss:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos13TextView2);
        t2.setText("Engineers use the head loss to understand how much energy a fluid needs to travel through a piping network. If there is too much head loss in a pipe system, the flow will not travel through the entire network.");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
