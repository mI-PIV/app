package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ScrollToTop;

public class Pos4_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos4);

        TextView t0 = (TextView)findViewById(R.id.pos4TextView0);
        t0.setText("Vorticity/Circulation");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos4TextView1);
        t1.setText("Vorticity and Circulation are two mathematical ideas which engineers use to describe the rotating of a fluid. Vorticity is a measure of the local rotation of a point in a fluid velocity field (the blue point below). An example is shown here:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos4TextView2);
        t2.setText("Vorticity is important as it allows engineers to quantify several important points of a flow field. For example, the vorticity around an airfoil indicates how much lift the airfoil produces.");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView) findViewById(R.id.pos4TextView3);
        t3.setText("\nMathematic Description:");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView) findViewById(R.id.pos4TextView4);
        t4.setText("Described as the curl of the velocity field, vorticity is typically denoted as \uD835\uDF14⃗⃗");
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t5 = (TextView) findViewById(R.id.pos4TextView5);
        t5.setText("Circulation is the line integral of the fluid velocity around a given closed curve. To visualize, imagine that circulation is the amount of a vector field that lies parallel to a given curve. An example is shown below:");
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t6 = (TextView) findViewById(R.id.pos4TextView6);
        t6.setText("Circulation is commonly denoted Γ. Mathematically, 2D circulation is described as the line integral of the velocity field tangent to a closed curve:");
        t6.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t7 = (TextView)findViewById(R.id.pos4TextView7);
        t7.setText("Where dl is the differential length of the given curve. This may be thought of as if you go around a circle in the flow, how much of the fluid velocity aligns with the direction of the circle.");
        t7.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t8 = (TextView)findViewById(R.id.pos4TextView8);
        t8.setText("\nConsiderations for mI-PIV:");
        t8.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t9 = (TextView) findViewById(R.id.pos4TextView9);
        t9.setText("While mI-PIV does not currently support vorticity, qualitatively observing the velocity field allows users to identify the presence of vorticity such as in the photo below:");
        t9.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5, t6, t7, t8, t9};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        fab = findViewById(R.id.fab);
        scrollView = findViewById(R.id.nestedScroll);

        ScrollToTop scrollToTop = new ScrollToTop(scrollView, fab);
        scrollToTop.scrollFunction();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
