package com.onrpiv.uploadmedia;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

public class Laser3 extends LaserSafetyDummy {
    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser3);
        TextView t0 = (TextView)findViewById(R.id.laserSafetyTextView0);
        t0.setText("Safe Use of Lasers in Classroom");
        t0.setTextSize(headerTextSize);
        TextView t4 = (TextView)findViewById(R.id.laserSafetyTextView4);
        String html4 = "When working with lasers, it’s critical to operate them safely so you don’t damage your equipment or harm yourself or someone nearby. The Laser Institute of America has defined an American National Standard for Safe Use of Lasers that is summarized here to understand the risks of working with lasers. There are three main things to consider: ";
        t4.setText(Html.fromHtml(html4));
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView t5 = (TextView)findViewById(R.id.laserSafetyTextView5);
        t5.setText("A laser’s potential for causing harm, the first consideration, is the main focus of laser safety. Lasers are categorized into classes based on their power. Class 1 lasers do not pose any danger because the entire laser apparatus is contained within an enclosed system, like a laser printer. Class 2 or 3R lasers are typically found in things like laser pointers available online. We strongly recommend against using Class 3B or 4 lasers with mI-PIV. ");
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView t6 = (TextView)findViewById(R.id.laserSafetyTextView6);
        String html3 = "Even if you are using a low powered laser, <b><u>do not look</u></b> directly at the beam.";
        t6.setText(Html.fromHtml(html3));
        t6.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView t7 = (TextView)findViewById(R.id.laserSafetyTextView7);
        t7.setText("When setting up your PIV experiment, consider the environment and pay special attention to all possible beam paths. You should be aware of unintentional paths the beam could take if the mount is unstable, the laser or table is bumped, or the laser reflects off a surface. When using a Class 1, 2, or 3R laser, an accidental beam path is not critical since these are relatively safe lasers to work with and your blink reflex should protect your eyes in the event of accidentally looking at the beam.");
        t7.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView[] textViews = {t4,t5,t6,t7};
        for(int i = 0; i<textViews.length; i++){
            textViews[i].setTextSize(paraTextSize);
        }
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
