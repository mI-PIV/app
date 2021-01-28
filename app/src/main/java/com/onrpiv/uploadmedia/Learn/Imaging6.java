package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.Learn.LearnImagingDummy;
import com.onrpiv.uploadmedia.R;

public class Imaging6 extends LearnImagingDummy {
    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging6);
        TextView t0 = (TextView)findViewById(R.id.learnImagingTextView0);
        t0.setText("Resolution");
        t0.setTextSize(headerTextSize);
        TextView t9 = (TextView)findViewById(R.id.learnImagingTextView9);
        String html7 = "The resolution of an image is a measurement of how many pixels make up a single image. Resolution is described by the number of pixels in each direction (x and y). For example, if an image has 720 pixels along the short axis, and 1280 pixels along the long axis, the image is 1280x720. (Short reference to resolution often picks the lowest number, so for 720x1280, the image would be 720p. 1080 video is 1920x1080.)<br><br>To see how resolution impacts an image, the following example shows a square image in increasing resolution.<br><br>This piece of a mI-PIV image was 1080x1080:";
        t9.setText(Html.fromHtml(html7));
        t9.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        //image
        TextView t32 = (TextView)findViewById(R.id.learnImagingTextView32);
        t32.setText("This is the same image with a resolution of 540x540:");
        //image
        TextView t33 = (TextView)findViewById(R.id.learnImagingTextView33);
        t33.setText("This is the same image with a resolution of 270x270:");
        //image
        TextView t34 = (TextView)findViewById(R.id.learnImagingTextView34);
        t34.setText("This is the same image with a resolution of 120x120:");
        //image
        TextView t10 = (TextView)findViewById(R.id.learnImagingTextView10);
        String html8 = "An interesting point to note is that the error in mI-PIV measurements is highly dependent on the resolution, as particles (and resultantly velocities) may only be “resolved” down to pixel size.";
        t10.setText(Html.fromHtml(html8));
        t10.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView[] textViews = {t9,t32,t33,t34,t10};
        for(int i = 0; i<textViews.length; i++){
            textViews[i].setTextSize(paraTextSize);
        }
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
