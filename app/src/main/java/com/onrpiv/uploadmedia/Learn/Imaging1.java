package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Imaging1 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging1);

        TextView t0 = (TextView)findViewById(R.id.imaging1TextView0);
        t0.setText("How Does a Digital Video Camera Work?");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.imaging1TextView2);
        String html1 = "\nFor PIV, the quality of images used determines the quality of the analysis results. To produce images, a digital camera needs a way to convert light, to the array of numbers described in “Learn about PIV”. This is done with a computer chip which collects and records light. These chips are separated into a finite number of sensors, whose output is decided by <b>bit depth</b>. (The number of <b>pixels</b>/sensors determines the <b>resolution</b> of the camera.) An example image of a camera sensor array is shown below:";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t2 = (TextView)findViewById(R.id.imaging1TextView3);
        String html2 = "When light contacts the “chip”, each sensor records the amount of light that has contacted it, which is also referred to as the sensor being “exposed”. The amount of time the sensor collects light is determined by the <b>ShutterSpeed</b> (with units of time in seconds).<br><br>As shown in the figure below, there are also several ways to adjust the light contacting the sensors. These adjustments are <b>focus</b> and <b>aperture</b>. Similarly, the sensitivity of the sensors may be adjusted, and this is referred to as ISO. As a further reading of these term definitions will show, PIV imaging requires a trade-off of the good and bad aspects of allowing more and less light through by adjusting shutter speed, aperture, and ISO.";
        t2.setText(Html.fromHtml(html2));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t3 = (TextView)findViewById(R.id.imaging1TextView4);
        String html3 = "To record a video, a camera repeats the imaging process described above multiple times. The images, referred to as “frames”, are recorded in finite time intervals. The rate at which these images are taken is referred to as <b>framerate</b> with units of frames per second (fps).";
        t3.setText(Html.fromHtml(html3));
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
