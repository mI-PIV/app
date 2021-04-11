package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.onrpiv.uploadmedia.R;

public class Laser2 extends LaserSafetyLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser2);

        TextView t0 = (TextView)findViewById(R.id.laserSafety2TextView0);
        t0.setText("Lasers in PIV");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.laserSafety2TextView1);
        String html1 = "Lasers are used in PIV to illuminate neutrally buoyant particles within a flow field. These particles are typically made of silica (glass) and are designed to scatter light so cameras can capture their position accurately. Check out the Learn About PIV page to learn more.";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.laserSafety2TextView2);
        t2.setText("To illuminate the particles, the laser passes through a cylindrical or half-circle lens (like a glass stir stick) that flattens the light into a plane.");
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.laserSafety2TextView3);
        String html2 = "You can only capture data on particles within the laser light sheet. It is safe to observe the particles moving within the plane when the laser is a sheet. They should look something like this:";
        t3.setText(Html.fromHtml(html2));
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        VideoView videoView = (VideoView)findViewById(R.id.laserSafety2VideoView0);
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.laser_safety_video);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
