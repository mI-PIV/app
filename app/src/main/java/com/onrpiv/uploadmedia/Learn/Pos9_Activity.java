package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos9_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos9);

        TextView t0 = (TextView)findViewById(R.id.pos9TextView0);
        t0.setText("Streamline");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos9TextView1);
        t1.setText("A streamline is a line in a fluid flow which is always tangent to the flow velocity. An example streamline is shown below in blue, where the velocity at a given point is shown in red:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos9TextView2);
        t2.setText("\nConsiderations for mI-PIV");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.pos9TextView3);
        t3.setText("By connecting the vectors of a mI-PIV output as shown in the figure above, the streamlines of a flow can be approximated by the user.");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
