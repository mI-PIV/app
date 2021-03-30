package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class Imaging8 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging8);

        TextView t0 = (TextView)findViewById(R.id.imaging8TextView0);
        t0.setText("Frame Rate");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.imaging8TextView1);
        String html1 = "Frame rate is the number of images (“frames”) taken per second in a video. It is important to note, that in a single camera, increasing the framerate (less time between images) means a shorter maximum shutter speed, as the shutter must open and shut for each frame to be recorded. To apply the framerate, the time between images is simply 1/framerate. For example, if there are 30 frames per second, then a frame is taken every 1/30 seconds or .033 seconds. See <b>Pixel</b> for using this framerate to identify displacements. With the displacement, the velocity may be found as the displacement divided by time.";
        t1.setText(Html.fromHtml(html1));
        t1.setTextSize(paraTextSize);
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
