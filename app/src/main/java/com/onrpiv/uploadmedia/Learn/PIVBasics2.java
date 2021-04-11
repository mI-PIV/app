package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class PIVBasics2 extends PIVBasicsLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pivbasics2);

        TextView t0 = (TextView)findViewById(R.id.QValueHeadingTextView);
        t0.setText("Q Value");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.QValueTextView1);
        t1.setText("The “Q” value, which is also referred to as the peak-to-peak ratio or a signal-to-noise ratio, is the highest peak value in the correlation plane divided by the next highest peak value (for more information on the correlation plane, see “How does PIV work?”). The lowest possible Q value is one, which corresponds to the situation when the highest peak and second highest peak values are equal. Figure 1 provides an example of a correlation plane with multiple peaks.");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.QValueTextView2);
        String html1 = "<b>Figure 1.</b> PIV correlation plane that contains multiple peaks: a primary peak and a secondary peak.<br><br>The Q value is one metric used to indicate the quality of the correlation for each output vector. Generally, the likelihood of the largest peak corresponding to the true displacement increases as the Q value increases. The mI-PIV app uses the Q value input by the user as a minimum threshold Q value to determine if output vectors are sufficiently reliable. If the calculated Q value for a given vector is less than the input threshold Q value, that vector is considered unreliable and is removed from the output vector field.";
        t2.setText(Html.fromHtml(html1));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.QValueTextView3);
        String boxedText = "<b>RULE OF THUMB:</b> Set an initial Q value threshold of 1.3. Users can relax this standard by decreasing Q (minimum of one), or tighten this standard by increasing Q.";
        t3.setText(Html.fromHtml(boxedText));
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3};
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextSize(paraTextSize);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

}
