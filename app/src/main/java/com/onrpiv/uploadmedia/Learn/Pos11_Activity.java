package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos11_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos11);

        TextView t0 = (TextView)findViewById(R.id.pos11TextView0);
        t0.setText("Bernoulli Equation");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos11TextView1);
        t1.setText("The Bernoulli equation describes steady, incompressible, inviscid flow along a streamline. When these assumptions are valid, the Bernoulli equation may be applied:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos11TextView2);
        t2.setText("This equation states that the sum of the flow energy (\uD835\uDC43\uD835\uDF0C), kinetic energy ((\uD835\uDC49^2)/2), and potential energy (\uD835\uDC54\uD835\uDC67) is constant along a streamline in steady, incompressible, inviscid flow. A common application of this is to pick two points along a streamline and estimate fluid properties using a known value.This is stated mathematically from points 1 and 2 as:");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
