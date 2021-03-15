package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos5_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos5);

        TextView t0 = (TextView)findViewById(R.id.pos5TextView0);
        t0.setText("Fluid");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos5TextView1);
        t1.setText("A fluid is defined as a substance which continually deforms under an applied shear stress or external force. A common phrasing of this is a substance which takes the shape of its container. There are several ways to classify fluids as well including the chemical phases of gas, liquid, and plasma. Fluids which have a linear viscous response to shear are referred to as Newtonian fluids. Fluids which are not Newtonian, are referred to as non-Newtonian fluids and are either shear-thinning (e.g. toothpaste) or shear-thickening (e.g. cornstarch and water). This distinction is important as the fluid properties and observations are dependent on phases. For example, the viscosity of a liquid is inversely proportional to temperature, whereas the viscosity of a gas is positively related as shown in the following figure:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos5TextView2);
        t2.setText("\nConsiderations for mI-PIV:");

        TextView t3 = (TextView)findViewById(R.id.pos5TextView3);
        t3.setText("mI-PIV is limited by the seeding and observation of flow fields. While mI-PIV labs can observe gases and fluids in low velocity flows, plasmas and high velocity flows require more expensive seeding and observation laboratory set-ups.");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
