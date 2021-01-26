package com.onrpiv.uploadmedia;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

public class Imaging7 extends LearnImagingDummy {
    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging7);
        TextView t0 = (TextView)findViewById(R.id.learnImagingTextView0);
        t0.setText("Focus");
        t0.setTextSize(headerTextSize);
        TextView t10 = (TextView)findViewById(R.id.learnImagingTextView10);
        String html8 = "The focus of a camera determines how ”sharp” an image is. Our eyes see this as blurred lines, and similarly, when the light is not focused on the image, as shown in the photos below, the particles are blurred. By placing the sensor, object of interest, and the lens at the appropriate distances, the image is brought into focus, and the light converges on the sensor as shown from android authority:";
        t10.setText(Html.fromHtml(html8));
        t10.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        //image
        TextView t11 = (TextView)findViewById(R.id.learnImagingTextView11);
        String html9 = "When the lens is too close to the sensor or too far, the image appears blurry. The correct focus may be visually identified through adjustment until the particles have crisp, clear edges as shown:<br><br>An out of focus image, where the particles are not easily distinguished from each other.";
        t11.setText(Html.fromHtml(html9));
        t11.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        TextView[] textViews = {t10,t11};
        for(int i = 0; i<textViews.length; i++){
            textViews[i].setTextSize(paraTextSize);
        }
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }
}
