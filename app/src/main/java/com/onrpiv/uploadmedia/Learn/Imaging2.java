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

public class Imaging2 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging2);

        TextView t0 = (TextView)findViewById(R.id.imaging2TextView0);
        t0.setText("Bit Depth");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.imaging2TextView1);
        String html1 = "Each sensor stores a number, which indicates the amount of light that contacted it while the sensor was exposed. Due to the limitations of computers, these numbers are limited by the amount of bits which describe each sensor reading, or the value of a pixel. To understand bit depth, we must first recognize that current computers may store only ones and zeros. The number of ones and zeros per sensor output (bits) determines bit depth. In other words, bit depth describes the number of values each pixel may take. If the image has bit depth of one, the image may take two values: 0 or 1. However, if an image has a bit depth of 2, four different values may be stored: 00, 01, 10, 11. Similarly, if an image has a bit depth of 3, eight different values may be stored: 000, 001, 010, 011, 100, 101, 110, and 111. This pattern continues where the number of values each pixel may take is described as:";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t2 = (TextView)findViewById(R.id.imaging2TextView2);
        t2.setText("Since most laboratory PIV setups use black and white images as they allow more information about the amount of light to be recorded, we will focus on black and white images. A discussion of color images is outside the scope of this page.\n\n Here is a mI-PIV image with a bit-depth of 256 (8 bits):");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t3 = (TextView)findViewById(R.id.imaging2TextView3);
        t3.setText("The same mI-PIV image with a bit-depth of 8 (3 bits):");
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t4 = (TextView)findViewById(R.id.imaging2TextView4);
        t4.setText("The same mI-PIV image with a bit-depth of 4 (2 bits):");
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t5 = (TextView)findViewById(R.id.imaging2TextView5);
        t5.setText("The same mI-PIV image with a bit-depth of 2 (1 bits):");
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t6 = (TextView)findViewById(R.id.imaging2TextView6);
        String html2 = "A greater bit-depth allows researchers to gather more information.This added information adds to the quality of correlations described in 'learn about PIV'.";
        t6.setText(Html.fromHtml(html2));
        t6.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5, t6};
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
