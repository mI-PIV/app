package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class PIVBasics5 extends PIVBasicsLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pivbasics5);

        TextView t0 = (TextView)findViewById(R.id.overlapHeadingTextView);
        t0.setText("Overlap");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.overlapTextView1);
        String html1 = "Interrogation region overlap determines the space between each consecutive interrogation region (IR) and, as a result, between output vectors. Generally, the overlap is the number of pixels which overlap between consecutive IRs. Figure 1 demonstrates this idea.";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.overlapTextView2);
        String html2 = "<b>Figure 1.</b> Demonstration of the concept of overlap, where the overlap is the length of the region IR 1 (Blue) and the following IR (Red) share.<br><br>Equation 1 describes the distance between IR centers (and therefore output vectors).";
        t2.setText(Html.fromHtml(html2));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.overlapTextView3);
        String html3 = "For example, a window size of 64 with an overlap of 32 pixels means each IR will be spaced 32 pixels apart (Figure 2 Part A). An increase in the overlap input decreases the space between outputs (Figure 1 Part 2). While decreasing the overlap decreases the density of output values (A), an increase in overlap also requires a longer computation time.";
        t3.setText(Html.fromHtml(html3));
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView)findViewById(R.id.overlapTextView4);
        String html4 = "<b>Figure 2.</b> Demonstration of the difference in overlap parameter choice. <b>A)</b> An overlap of 32 pixels, and <b>B)</b> an overlap of 0 pixels.";
        t4.setText(Html.fromHtml(html4));
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t3, t4};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

}
