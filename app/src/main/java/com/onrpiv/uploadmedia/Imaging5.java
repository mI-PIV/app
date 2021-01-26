package com.onrpiv.uploadmedia;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

public class Imaging5 extends LearnImagingDummy {
    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging5);

        TextView t0 = (TextView)findViewById(R.id.learnImagingTextView0);
        t0.setText("Shutter Speed");
        t0.setTextSize(headerTextSize);
        TextView t8 = (TextView)findViewById(R.id.learnImagingTextView8);
        String html6 = "To allow light to reach the sensor, the shutter has to open and then close(or in the case of electronic shutter, the sensors must be turned on and then off). The amount of time the shutter spends open is referred to as shutter speed and is measured by the amount of time open(on)in seconds.<br><br>Shutter speed has several important implications in PIV.One important consideration is particle image streaking or simply <b>streaking</b>. The amount of distance a particle moves while the shutter is open is recorded by the camera.As PIV measures movement between frames, particles which are “streaked” make the movement between frames harder to correlate.By decreasing the shutter speed,streaking is reduced. However, the amount of light which hits each sensor is decreased, as the time each sensor is exposed is decreased. The following images have a decreasing shutter speed. To keep the particles visible,the ISO has been adjusted across these images.<br>";
        t8.setText(Html.fromHtml(html6));
        t8.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView t36 = (TextView)findViewById(R.id.learnImagingTextView38);
        t36.setText("Shutter Speed: 1/30s, ISO: 800");
        //image
        TextView t22 = (TextView)findViewById(R.id.learnImagingTextView22);
        t22.setText("Shutter Speed: 1/50s, ISO: 1200");
        //image
        TextView t23 = (TextView)findViewById(R.id.learnImagingTextView23);
        t23.setText("Shutter Speed: 1/80s, ISO: 1200");
        //image
        TextView t24 = (TextView)findViewById(R.id.learnImagingTextView24);
        t24.setText("Shutter Speed: 1/125s, ISO: 1600");
        //image
        TextView t25 = (TextView)findViewById(R.id.learnImagingTextView25);
        t25.setText("Shutter Speed: 1/200s, ISO: 2400");
        //image

        TextView t26 = (TextView)findViewById(R.id.learnImagingTextView26);
        t26.setText("This following set of images is of a region of PIV particles which are not moving, to show how shutter speed affects the amount of light which reaches the sensors.\n");
        t26.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView t37 = (TextView)findViewById(R.id.learnImagingTextView39);
        t37.setText("Shutter Speed: 1/30s");
        //image
        TextView t27 = (TextView)findViewById(R.id.learnImagingTextView27);
        t27.setText("Shutter Speed: 1/50s");
        //image
        TextView t28 = (TextView)findViewById(R.id.learnImagingTextView28);
        t28.setText("Shutter Speed: 1/80s");
        //image
        TextView t29 = (TextView)findViewById(R.id.learnImagingTextView29);
        t29.setText("Shutter Speed: 1/125s");
        //image
        TextView t30 = (TextView)findViewById(R.id.learnImagingTextView30);
        t30.setText("Shutter Speed: 1/200s");
        //image
        TextView t31 = (TextView)findViewById(R.id.learnImagingTextView31);
        t31.setText("Shutter Speed: 1/320s");
        //image
        TextView t9 = (TextView)findViewById(R.id.learnImagingTextView9);
        String html7 = "For these reasons, the shutter speed should be reduced as much as necessary to eliminate streaking, while still allowing the particles to be seen. The light is also impacted by the aperture and ISO. Another important consideration is that as the framerate decreases,the maximum shutter speed decreases, as the shutter must open and shut for each frame to be recorded.";
        t9.setText(Html.fromHtml(html7));
        t9.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView[] textViews = {t8,t36,t22,t23,t24,t25,t26,t37,t27,t28,t29,t30,t31,t9,};
        for(int i = 0; i<textViews.length; i++){
            textViews[i].setTextSize(paraTextSize);
        }
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
