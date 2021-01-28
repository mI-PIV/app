package com.onrpiv.uploadmedia;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

public class Imaging9 extends LearnImagingDummy {
    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging9);
        TextView t0 = (TextView)findViewById(R.id.learnImagingTextView0);
        t0.setText("Aperture");
        t0.setTextSize(headerTextSize);
        TextView t12 = (TextView)findViewById(R.id.learnImagingTextView12);
        String html10 = "The aperture is a variably sized opening where the light passes through before reaching the sensors. The aperture allows more and less light to reach the sensors and is another way to adjust the brightness of the final image as shown below.";
        t12.setText(Html.fromHtml(html10));
        t12.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        //image
        TextView t13 = (TextView) findViewById(R.id.learnImagingTextView13);
        t13.setText("However, the larger the aperture, the more narrow the ”depth of field” or the distance particles remain in focus. The larger the depth of field, the longer the band of an image that remains in focus. Aperture is measured by “fstop” where the larger the aperture, the smaller the fstop value.");
        t13.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        //image
        TextView[] textViews = {t12,t13};
        for(int i = 0; i<textViews.length; i++){
            textViews[i].setTextSize(paraTextSize);
        }
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
