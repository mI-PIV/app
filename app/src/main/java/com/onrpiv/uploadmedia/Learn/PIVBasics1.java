package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ScrollToTop;

public class PIVBasics1 extends PIVBasicsLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pivbasics1);

        TextView t0 = (TextView)findViewById(R.id.headingTextview);
        t0.setText("PIV Basics");
        t0.setTextSize(headerTextSize);

        TextView t1 = (TextView)findViewById(R.id.pivBasicsTextView1);
        t1.setText("Particle Image Velocimetry (PIV) is an optical experimental technique engineers use to both visualize and measure fluid flow fields (i.e., liquid or gas). A common laboratory set up for PIV is depicted below:");
        t1.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t2 = (TextView)findViewById(R.id.pivBasicsTextView2);
        String html1 = "<b>Figure 1.</b> Example laboratory PIV set-up, where a camera images a laser light sheet that is illuminating a flow field seeded with particles.<br><br>As shown in the figure above, laser sheet optics (i.e., a cylindrical lens) focus the collimated laser beam into a thin laser sheet. The laser sheet illuminates neutrally buoyant particles (i.e., seeds) that have been introduced into the flow. These small (1-100 \uD835\uDF07m) seeds both follow the flow field and effectively scatter the laser light so that they can be seen and imaged. A digital camera (directed orthogonal to the laser sheet plane) images the particles in the plane as shown in Figure 2 below. The velocity can be calculated by taking the displacement of the particles divided by a known time step. In general, the particles shouldn’t move too much, the flow field being images should be the same between time steps and the time should be small enough that the particles have only moved a few particle diameters.";
        t2.setText(Html.fromHtml(html1));
        t2.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t3 = (TextView)findViewById(R.id.pivBasicsTextView3);
        String html2 = "<b>Figure 2.</b> Image of a flow field that was illuminated with a laser light-sheet and imaged by a camera and the mi-PIV application. The neutrally buoyant particles in the flow field appear white as they scatter the laser light.<br>";
        t3.setText(Html.fromHtml(html2));
        t3.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t4 = (TextView)findViewById(R.id.pivBasicsTextView4);
        t4.setText("By taking two images of the same space of a flow field a small known time apart, the displacement of the particle position over time (i.e., velocity) can be computed.");
        t4.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t5 = (TextView)findViewById(R.id.pivBasicsTextView5);
        t5.setText("\nPIV Algorithms\n");
        t5.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t6 = (TextView)findViewById(R.id.pivBasicsTextView6);
        t6.setText("Instead of tracking the movement of each individual particle (known as PTV), PIV algorithms segment each of the two images into smaller images referred to as interrogation regions.");
        t6.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t7 = (TextView)findViewById(R.id.pivBasicsTextView7);
        String html3 = "<b>Figure 3.</b> Main image 1 (from Figure 2) partitioned into interrogation regions. <br><br>Basic PIV algorithms employ statistical correlation methods to identify the most likely location of a set of particles as they move within an interrogation region in the first image to an interrogation window in the second image. The “search area” is the interrogation region in the second image. The translation of a set of particles is identified by finding the change in x and y location from the first image to the second image. PIV algorithms identify the velocity of the fluid in each region by dividing this translation by the prescribed time between images.\n\nMore accurate PIV measurements result when particle movements are contained within overlapping interrogation regions, since a small search radius increases the likelihood of maintaining valid correlations (i.e., correlating the translation of the same particles). The likelihood of a poor or erroneous correlation increases as the “search area” increases. In other words, the farther you move from the particles’ initial position in the first interrogation region, the more likely you are to correlate to the wrong set of particles in the second interrogation region.";
        t7.setText(Html.fromHtml(html3));
        t7.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t8 = (TextView)findViewById(R.id.pivBasicsTextView8);
        t8.setText("\nCross Correlation Computations");
        t8.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t9 = (TextView)findViewById(R.id.pivBasicsTextView9);
        t9.setText("\nTo identify the statistically “most likely” movement of particles between the interrogation regions, a cross correlation formula is used. Direct cross correlation is the most straightforward method, albeit slow computationally. The equation for direct cross correlation is:");
        t9.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t10 = (TextView)findViewById(R.id.pivBasicsTextView10);
        t10.setText("where A1 is the matrix of numerical greyscale pixel values for the first interrogation region, A2 is the matrix of numerical greyscale pixel values for the second interrogation region, D is the length of A1, i and j are the row and column indices respectively, and r and s describe the translation of the interrogation region in the y and x directions. The correlation matrix R contains values for a given r and s. The R matrix values are the sum of the element wise products of the numerical pixel values for the first interrogation region and the numerical pixel values for the second interrogation region when shifted by r and s. By building the R matrix in this manner, the values of r and s that maximize R describe the most likely translation of the interrogation region. The movement of the flow (i.e., magnitude and direction), then, is indicated by the displacement vector drawn from the center of the image to the pixel location of the peak value in the R matrix. The values r and s are typically held within ±D/2 to increase the likelihood of valid correlations and to decrease computational time.\nAn example of a direct cross correlation computation is shown below. Each pixel is either a one (striped) or zero (white). (While PIV uses a wider range of greyscale values coming from the digital images, binary values are used here for illustration purposes only.)");
        t10.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t11 = (TextView)findViewById(R.id.pivBasicsTextView11);
        String html4 = "<b>Figure 4.</b> Example interrogation regions for cross correlation [1]. The first image at t shows an area (A1) that will be used to find a correlation in the area A2 in image t+\uF044t.<br><br>Figure 5 shows how the R matrix is built by overlapping the A1 (red) over a region of A2 (green, region is decided by the range of r and s), multiplying the overlapping values, and adding each product. The sum of these products is the R value at that specific r and s.";
        t11.setText(Html.fromHtml(html4));
        t11.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView t12 = (TextView)findViewById(R.id.pivBasicsTextView12);
        String html5 = "<b>Figure 5.</b> Visual demonstration of cross correlation R matrix development. The R matrix (left) is the sum of overlapping pixels per r and s. <b>Top (r = -2, s = -2): </b>one pair of pixels have an overlapping particle, <b>Middle (r = 0, s = 0):</b> two pixels pairs have overlapping particles, <b>Bottom (r = 2, s = -2):</b> five pixel pairs have overlapping particles.<br><br>As shown in above figure, the pixels which overlap are shaded horizontally and vertically, indicating a product of one in the summation (due to the lack of greyscale values beyond zero and one). The most likely movement of the particles is down two pixels, and left two pixels, or Rmax = R(r = 2, s = -2) = 5.<br><br><b>References:</b><br><br>[1] B. L. Smith and D. R. Neal, “Particle Image Velocimetry,” Part. Image Velocim., p. 27, 2016.";
        t12.setText(Html.fromHtml(html5));
        t12.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        TextView[] textViews = {t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12};
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
