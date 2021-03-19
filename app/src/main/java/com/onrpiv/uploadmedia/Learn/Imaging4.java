package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ScrollToTop;

public class Imaging4 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging4);

        TextView t0 = (TextView)findViewById(R.id.imaging4TextView0);
        t0.setText("ISO");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView) findViewById(R.id.imaging4TextView1);
        String html1 = "ISO refers to each sensor’s sensitivity to light. By increasing the ISO, the sensitivity of the camera is increased, and each sensor gathers more light. However, the “noise,” or the amount of light which does not come from the PIV particles, also increases as ISO is increased.<br> An example of the influence ISO has on a PIV image is shown below, where the ISO increases moving down the page.<br>";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.imaging4TextView2);
        t2.setText("ISO:200");

        //image
        TextView t3 = (TextView)findViewById(R.id.imaging4TextView3);
        t3.setText("ISO:400");

        //image
        TextView t4 = (TextView)findViewById(R.id.imaging4TextView4);
        t4.setText("ISO:800");

        //image
        TextView t5 = (TextView)findViewById(R.id.imaging4TextView5);
        t5.setText("ISO:1200");

        //image
        TextView t6 = (TextView)findViewById(R.id.imaging4TextView6);
        t6.setText("ISO:1600");

        //image
        TextView t7 = (TextView)findViewById(R.id.imaging4TextView7);
        t7.setText("ISO:2400");

        //image
        TextView t8 = (TextView)findViewById(R.id.imaging4TextView8);
        String html2 = "Based on the increase in noise paired with an increase in ISO, the ISO should be adjusted low enough to avoid bad correlations due to noise, and high enough to achieve good correlations from adequately illuminated particles.";
        t8.setText(Html.fromHtml(html2));
        t8.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5, t6, t7, t8};
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
