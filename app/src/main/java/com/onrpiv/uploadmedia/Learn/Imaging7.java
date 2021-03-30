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

public class Imaging7 extends LearnImagingLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imaging7);

        TextView t0 = (TextView)findViewById(R.id.imaging7TextView0);
        t0.setText("Focus");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.imaging7TextView1);
        String html1 = "The focus of a camera determines how ”sharp” an image is. Our eyes see this as blurred lines, and similarly, when the light is not focused on the image, as shown in the photos below, the particles are blurred. By placing the sensor, object of interest, and the lens at the appropriate distances, the image is brought into focus, and the light converges on the sensor as shown from android authority:";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        //image
        TextView t2 = (TextView)findViewById(R.id.imaging7TextView2);
        String html2 = "When the lens is too close to the sensor or too far, the image appears blurry. The correct focus may be visually identified through adjustment until the particles have crisp, clear edges as shown:<br><br>An out of focus image, where the particles are not easily distinguished from each other.";
        t2.setText(Html.fromHtml(html2));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2};
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
