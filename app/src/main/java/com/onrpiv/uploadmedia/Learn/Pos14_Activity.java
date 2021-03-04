package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos14_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos14);

        TextView t0 = (TextView)findViewById(R.id.pos14TextView0);
        t0.setText("Common Assumptions");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos14TextView1);
        String html1 = "<u><b>Steady flow</u></b>: When a flow is assumed to be steady, the flow characteristics are independent of time. This assumption is made for situations where the flow is reasonably approximated as unchanging in time. For example, flow in a pipe is often assumed steady, although the flow may change slightly as water flows through a pipe. The flow is assumed to be approximately the same despite when observed.";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos14TextView2);
        String html2 = "<br><b><u>Incompressible</u></b>: When a flow is incompressible, the density does not change throughout the fluid. These allow simplification of many physical laws. Such as for steady, incompressible flow, the divergence theorem:";
        t2.setText(Html.fromHtml(html2));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.pos14TextView3);
        t3.setText("May be simplified:");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView)findViewById(R.id.pos14TextView4);
        String html3 = "<u><b>Inviscid</b></u>: An inviscid flow assumes that the effects of viscosity are negligible. In high Reynolds number flows such as over an aircraft wing, this assumption results in small error, and allows for simplification of many physical laws.<br>";
        t4.setText(Html.fromHtml(html3));
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t5 = (TextView)findViewById(R.id.pos14TextView5);
        String html4 = "<u><b>1D and 2D flows</u></b>: For certain flow geometries, assuming the flow is uniform in certain directions simplifies the problem. For example, in incompressible pipe flow, the flow velocity along the pipe is approximately constant. As well, for a cylindrical pipe, the velocity will only change in the radial direction, and is symmetric about the center of the pipe. These allow the flow velocity to simplify in all directions except along the radius.<br>";
        t5.setText(Html.fromHtml(html4));
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t6 = (TextView)findViewById(R.id.pos14TextView6);
        String html5 = "<u><b>No-Slip Boundary</u></b>: This assumption states that the flow velocity along a boundary is zero (or the velocity of the boundary if the surface is moving). This allows engineers to find solutions to flow field by setting boundary conditions.<br>";
        t6.setText(Html.fromHtml(html5));
        t6.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5, t6};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
