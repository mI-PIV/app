package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.Learn.FluidGlossary;
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

        TextView t3 = (TextView)findViewById(R.id.pos11TextView3);
        t3.setText("\nConsiderations for mI-PIV:");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView)findViewById(R.id.pos11TextView4);
        t4.setText("An example of using the Bernoulli equation for mI-PIV is in the horizontal jet experiment. By knowing the height of the water line in the pipe, and the depth of the pipe, the user can approximate the velocity at the pipe outlet:");
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t5 = (TextView)findViewById(R.id.pos11TextView5);
        t5.setText("Adding relevant terms into the Bernoulli equation shows that the velocity at point 2 will be approximately described as shown:");
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t6 = (TextView)findViewById(R.id.pos11TextView6);
        t6.setText("Solving for the velocity yields:");
        t6.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5, t6};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
