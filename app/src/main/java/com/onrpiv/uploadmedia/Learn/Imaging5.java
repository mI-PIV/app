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

public class Imaging5 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging5);

        TextView t0 = (TextView)findViewById(R.id.imaging5TextView0);
        t0.setText("Shutter Speed");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.imaging5TextView1);
        String html6 = "To allow light to reach the sensor, the shutter has to open and then close (or in the case of electronic shutter, the sensors must be turned on and then off). The amount of time the shutter spends open is referred to as shutter speed and is measured by the amount of time open (on) in seconds.<br><br>Shutter speed has several important implications in PIV. One important consideration is particle image streaking or simply <b>streaking</b>. The amount of distance a particle moves while the shutter is open is recorded by the camera. As PIV measures movement between frames, particles which are “streaked” make the movement between frames harder to correlate. By decreasing the shutter speed, streaking is reduced. However, the amount of light which hits each sensor is decreased, as the time each sensor is exposed is decreased. The following images have a decreasing shutter speed. To keep the particles visible, the ISO has been adjusted across these images.<br>";
        t1.setText(Html.fromHtml(html6));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.imaging5TextView2);
        t2.setText("Shutter Speed: 1/30s, ISO: 800");

        //image
        TextView t3 = (TextView)findViewById(R.id.imaging5TextView3);
        t3.setText("Shutter Speed: 1/50s, ISO: 1200");

        //image
        TextView t4 = (TextView)findViewById(R.id.imaging5TextView4);
        t4.setText("Shutter Speed: 1/80s, ISO: 1200");

        //image
        TextView t5 = (TextView)findViewById(R.id.imaging5TextView5);
        t5.setText("Shutter Speed: 1/125s, ISO: 1600");

        //image
        TextView t6 = (TextView)findViewById(R.id.imaging5TextView6);
        t6.setText("Shutter Speed: 1/200s, ISO: 2400");

        //image
        TextView t7 = (TextView)findViewById(R.id.imaging5TextView7);
        t7.setText("This following set of images is of a region of PIV particles which are not moving, to show how shutter speed affects the amount of light which reaches the sensors.\n");
        t7.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t8 = (TextView)findViewById(R.id.imaging5TextView8);
        t8.setText("Shutter Speed: 1/30s");

        //image
        TextView t9 = (TextView)findViewById(R.id.imaging5TextView9);
        t9.setText("Shutter Speed: 1/50s");

        //image
        TextView t10 = (TextView)findViewById(R.id.imaging5TextView10);
        t10.setText("Shutter Speed: 1/80s");

        //image
        TextView t11 = (TextView)findViewById(R.id.imaging5TextView11);
        t11.setText("Shutter Speed: 1/125s");

        //image
        TextView t12 = (TextView)findViewById(R.id.imaging5TextView12);
        t12.setText("Shutter Speed: 1/200s");

        //image
        TextView t13 = (TextView)findViewById(R.id.imaging5TextView13);
        t13.setText("Shutter Speed: 1/320s");

        //image
        TextView t14 = (TextView)findViewById(R.id.imaging5TextView14);
        String html1 = "For these reasons, the shutter speed should be reduced as much as necessary to eliminate streaking, while still allowing the particles to be seen. The light is also impacted by the aperture and ISO. Another important consideration is that as the framerate decreases, the maximum shutter speed decreases, as the shutter must open and shut for each frame to be recorded.";
        t14.setText(Html.fromHtml(html1));
        t14.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14};
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
