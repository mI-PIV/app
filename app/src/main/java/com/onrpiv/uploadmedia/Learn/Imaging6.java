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

public class Imaging6 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging6);

        TextView t0 = (TextView)findViewById(R.id.imaging6TextView0);
        t0.setText("Resolution");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.imaging6TextView1);
        String html1 = "The resolution of an image is a measurement of how many pixels make up a single image. Resolution is described by the number of pixels in each direction (x and y). For example, if an image has 720 pixels along the short axis, and 1280 pixels along the long axis, the image is 1280x720. (Short reference to resolution often picks the lowest number, so for 720x1280, the image would be 720p. 1080 video is 1920x1080.)<br><br>To see how resolution impacts an image, the following example shows a square image in increasing resolution.<br><br>This piece of a mI-PIV image was 1080x1080:";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t2 = (TextView)findViewById(R.id.imaging6TextView2);
        t2.setText("This is the same image with a resolution of 540x540:");

        //image
        TextView t3 = (TextView)findViewById(R.id.imaging6TextView3);
        t3.setText("This is the same image with a resolution of 270x270:");

        //image
        TextView t4 = (TextView)findViewById(R.id.imaging6TextView4);
        t4.setText("This is the same image with a resolution of 120x120:");

        //image
        TextView t5 = (TextView)findViewById(R.id.imaging6TextView5);
        String html2 = "An interesting point to note is that the error in mI-PIV measurements is highly dependent on the resolution, as particles (and resultantly velocities) may only be “resolved” down to pixel size.";
        t5.setText(Html.fromHtml(html2));
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5};
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
