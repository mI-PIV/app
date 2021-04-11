package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class PIVBasics4 extends PIVBasicsLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pivbasics4);

        TextView t0 = (TextView)findViewById(R.id.medianFilterHeadingTextView);
        t0.setText("Median Filter");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.medianFilterTextView1);
        String html1 = "The median filter parameter specifies the median threshold value that is used for identifying vectors which may be unreliable. This method for identifying unreliable vectors is based on the idea that the fluid flow is generally smooth and particles near each other move in a similar direction.<br><br>As shown in Figure 1, the median filter examines the eight vectors that surround each output vector, and gauges if the output vector is reasonably similar to the vectors around it.";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.medianFilterTextView2);
        String html2 = "<b>Figure 1.</b> Demonstration of the median threshold concept where the vector of interest is compared to the vectors adjacent. The green arrow is generally similar in direction and magnitude to the eight vectors around it. The red arrow is less similar (in direction) to the eight neighboring vectors, and therefore less likely to occur.<br><br>If the vector is judged as unlikely to occur (and therefore unreliable) by the median filter, it is removed from the output.";
        t2.setText(Html.fromHtml(html2));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.medianFilterTextView3);
        String boxedText = "<b>RULE OF THUMB:</b> Set a median threshold value of two. Increasing the median threshold value will result in a less stringent comparison and decreasing the median parameter will result in a more stringent comparison.";
        t3.setText(Html.fromHtml(boxedText));
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}

