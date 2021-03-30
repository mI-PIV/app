package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.ImageView;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos1_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos1);

        TextView t0 = (TextView)findViewById(R.id.pos1TextView0);
        t0.setText("Boundary Layer");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView) findViewById(R.id.pos1TextView1);
        t1.setText("The boundary layer is the thin fluid layer which forms between a flowing fluid and a surface due to the no-slip condition (the flow velocity at a surface is 0). To satisfy the difference in the freestream and surface velocities, the region of fluid between the freestream and surface whose velocity ranges from 0 to 99% (typically) of the freestream velocity is referred to as the boundary layer. An example of the boundary layer (gray) along a flat plate is shown in the photo below:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        ImageView iv = (ImageView)findViewById(R.id.pos1ImageView1);
        iv.setImageResource(R.drawable.new_boundarylayer);

        TextView t2 = (TextView) findViewById(R.id.pos1TextView2);
        t2.setText("The thickness of a boundary layer is dependent on the surface geometry and Reynolds number. For a flat wall with laminar flow, the boundary layer thickness  \uD835\uDEFF, at a distance \uD835\uDC65 down the plate in the freestream direction, may be solved by the following equation:");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView textView0 = (TextView) findViewById(R.id.pos1TextView3);
        textView0.setText("For a flat plate with turbulent flow, the boundary layer thickness may be described by the following:");
        textView0.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView) findViewById(R.id.pos1TextView4);
        t3.setText("\nConsiderations for mI-PIV:");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView) findViewById(R.id.pos1TextView5);
        t4.setText("Surfaces contacting the fluid will also likely be illuminated by the laser. Since particles will not be distinguishable from an illuminated boundary, boundary layers are difficult to observe in PIV. The photo below shows an example of this in pipe flow. The edge of the pipe is illuminated by the laser, making the velocity vectors null.");
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

    }
}
