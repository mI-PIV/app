package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ScrollToTop;

public class PIVBasics3 extends PIVBasicsLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pivbasics3);

        TextView t0 = (TextView)findViewById(R.id.windowSizeHeadingTextView);
        t0.setText("Interrogation Region or Window Size");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.windowSizeTextView1);
        String html1 = "Choosing an appropriate interrogation region or “window” size in PIV involves trade-offs. A larger window increases the number of particles per correlation and can increase the likelihood of finding a valid correlation. Increasing the window size, however, also decreases the spatial resolution of the displacement of the particles in each interrogation region (IR). This means that if the particle displacements vary within a given IR, the output vector may be less representative of the particles near the center of the IR (where the output vector is returned/placed).<br><br>A demonstration of these tradeoffs is shown in Figure 1. Imagine a pair of overlaid images with particle locations in the first image shown in red, and particle images in the second image shown in gold. To make the displacement of each particle easier to track, a black line is drawn from each particle location in the first image to the particle location in the second image.";
        t1.setText(Html.fromHtml(html1));
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.windowSizeTextView2);
        String html2 = "<b>Figure 1.</b> A set of particle locations on two overlaid images. Image one locations are represented by the red dots, and image two locations are represented by the yellow dots. The black lines indicate that the movement from image one to image two of each particle.<br><br>Now, let’s compare three interrogation region sizes as shown in Figure 2.";
        t2.setText(Html.fromHtml(html2));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.windowSizeTextView3);
        String html3 = "<b>Figure 2.</b> Demonstration of the effects of varying window sizes (gray squares) on the resultant vector output (green arrow).<br><br>First, note that the IR shown in part A of Figure 2 is too small, since it has less than five particles, it has a higher likelihood (than an IR with more than five particles,) of particles exiting the IR and becoming “lost” between the first and second images. These lost particles are likely to lead to unreliable vector outputs. The vector output of small IRs, however, can represent the overall displacement near the center of the IR well, as long as it contains an adequate number of particles which appear in both images.<br><br>The IR shown in Part B of Figure 2 appears ideal, as it balances the need to capture an adequate number of particles between the first and second image to ensure a valid vector output located at the center of the correlation plane.";
        t3.setText(Html.fromHtml(html3));
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView)findViewById(R.id.windowSizeTextView4);
        String boxedText1 = "<b>RULE OF THUMB:</b> Interrogation regions should contain at least five particles to result in a good correlation value.";
        t4.setText(Html.fromHtml(boxedText1));
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t5 = (TextView)findViewById(R.id.windowSizeTextView5);
        String html4 = "The IR shown in Part C of Figure 2 is too large. Although the number and percentage of particles present in both the first and second images is high (i.e., larger than five), the size of the IR results in the single vector output representing a large area. Using one vector output to represent the aggregate displacements of all particles contained in the large IR may provide unreliable results.<br><br>If desired, reduce the window size (i.e., to less than 64 pixels) to increase your resolution. However, be cautious of the output values as the window size and the number of particles in each region decreases. As well, the displacement between images relative to the IR size will increase.";
        t5.setText(Html.fromHtml(html4));
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t6 = (TextView)findViewById(R.id.windowSizeTextView6);
        String boxedText2 = "<b>RULE OF THUMB:</b> Start a mI-PIV experiment with a window size of 64 pixels, and if you decrease the window size, maintain a maximum particle displacement below 1/4 the window size.";
        t6.setText(Html.fromHtml(boxedText2));
        t6.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t7 = (TextView)findViewById(R.id.windowSizeTextView7);
        String html5 = "Reducing the window size too far can result in many unreliable (i.e., “spurious”) vectors (which will not qualitatively seem to fit the fluid movement.)";
        t7.setText(Html.fromHtml(html5));
        t7.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t3, t4, t5, t6, t7};
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
