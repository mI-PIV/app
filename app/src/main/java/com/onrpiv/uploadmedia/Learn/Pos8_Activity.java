package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Pos8_Activity extends FluidGlossary {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos8);

        TextView t0 = (TextView)findViewById(R.id.pos8TextView0);
        t0.setText("Velocity Profile");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pos8TextView1);
        t1.setText("A velocity profile describes the magnitude of a flow velocity as a function of a specified direction variable. For example, the velocity profile of a laminar flow in a pipe is shown in the following image:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pos8TextView2);
        t2.setText("Here the magnitude of the velocity in the Z direction is plotted along the r direction.");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.pos8TextView3);
        t3.setText("\nConsiderations for mI-PIV");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView)findViewById(R.id.pos8TextView4);
        t4.setText("mI-PIV provides the user with a velocity field rather than a velocity profile. To find the velocity profile, the user must hold one axis constant, and plot the velocity in the other direction. For example, to find the profile above given a field of pipe flow.");
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
