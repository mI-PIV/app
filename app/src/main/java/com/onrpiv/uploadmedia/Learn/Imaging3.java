package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Imaging3 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging3);

        TextView t0 = (TextView)findViewById(R.id.imaging3TextView0);
        t0.setText("Pixel");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.imaging3TextView1);
        t1.setText("A pixel is a small square area of light which makes up an image. The amount of pixels in an image determines the image resolution. An example of how pixels make an image is shown below:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.imaging3TextView2);
        String html1 = "To use pixels in PIV, we calibrate the image. Commonly, image calibration begins with a photo taken by the camera of something of a known length (e.g., a ruler). By identifying the number of pixels that is equivalent to the known length of the item, experimenters can determine a conversion between pixels and length (e.g. meters, inches). For example, consider a calibrated image that has 100 pixels per inch, if an area of fluid moves 10 pixels between frames, we find that the displacement is 10/100 inches or 0.1 inches.To convert framerate to time, and find velocity see the framerate term description. Note: the calibration is only valid for the camera and laser sheet at a set distance apart, and the calibration is only valid along the plane used to calibrate the image.";
        t2.setText(Html.fromHtml(html1));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
