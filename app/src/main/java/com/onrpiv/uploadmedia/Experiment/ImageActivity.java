package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.LightBulb;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;
import com.onrpiv.uploadmedia.pivFunctions.PivRunner;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.Serializable;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class ImageActivity extends AppCompatActivity {
    Button parameters, compute, display, pickImageMultiple, review;
//    Button lightbulb1, lightbulb2, lightbulb3;
//    ArrayList<Button> allButtons;
    private Uri fileUri;
    private String userName;
    private PivParameters pivParameters;
    private File frame1File;
    private File frame2File;
    private PivResultData resultData;

    private LightBulb imagePair;
    private LightBulb imageCorrelation;
    private LightBulb computePIV;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init buttons
        pickImageMultiple = (Button) findViewById(R.id.pickImageMultiple);
        parameters = (Button) findViewById(R.id.parameters);
        compute = (Button) findViewById(R.id.compute);
        display = (Button) findViewById(R.id.display);
        review = (Button) findViewById(R.id.Review);

        // Get the transferred data from source activity.
        Intent userNameIntent = getIntent();
        userName = userNameIntent.getStringExtra("UserName");

        OpenCVLoader.initDebug();

        context = getApplicationContext();
        imagePair = new LightBulb(context, findViewById(R.id.imageActivityRelativeLayout));
        imagePair.setLightBulbOnClick("Image Pair",
                "You need to select two images to compute movement of the particles from the first to the second image.",
                getWindow());
        //imagePair.setPosition(100, 0);

//        imageCorrelation = new LightBulb(context, review);
//        imageCorrelation.setLightBulbOnClick("Image Correlation", "Review the images selected in \"select an image pair\" and consider whether the images will result in a useful PIV output.", new PIVBasics3(), "Learn More");
//
//        computePIV = new LightBulb(context, compute);
//        computePIV.setLightBulbOnClick("Compute PIV", "Compute PIV computes the velocity field between the first and second image from \"Select An Image Pair\" according to the parameters in \"Input PIV Parameters\". For more information see: ", new PIVBasicsLayout(), "Learn More");

        // all the buttons (that aren't lightbulbs)
//        allButtons = new ArrayList<>();
//        allButtons.add(pickImageMultiple);
//        allButtons.add(parameters);
//        allButtons.add(compute);
//        allButtons.add(display);
//        allButtons.add(review);

        //popupWindowImageLayoutRun();
        // Add popupwindow here
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick_MultipleImages(View view) {
        final PivFrameSelectionPopup frameSelectionPopup = new PivFrameSelectionPopup(ImageActivity.this,
                userName);

        // create listener for frame selection save button
        View.OnClickListener saveListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frame1File = frameSelectionPopup.frame1Path;
                frame2File = frameSelectionPopup.frame2Path;

                review.setEnabled(true);
                parameters.setEnabled(true);
                pickImageMultiple.setBackgroundColor(Color.parseColor("#00CC00"));
                frameSelectionPopup.dismiss();
            }
        };

        frameSelectionPopup.setSaveListener(saveListener);
        frameSelectionPopup.show();
    }

    // creates a popupwindow that gives information and possibly a link to somewhere else in the app
//    private void popupWindowImageLayoutRun()
//    {
//        context = getApplicationContext();
        // tooltips variables
        //    private PopupWindow popupWindow;
//        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.imageActivityRelativeLayout);

//        lightbulb1 = (Button) findViewById(R.id.lightbulbImageLayout1);
//        lightbulb2 = (Button) findViewById(R.id.lightbulbImagelayout2);
//        lightbulb3 = (Button) findViewById(R.id.lightbulbImageLayout3);
//        lightbulb1.setHovered(true);

//        final String title1 = "Image Pair";
//        final String title2 = "Image Correlation";
//        final String title3 = "Compute PIV";
//
//        final String message1 = "You need to select two images to compute movement of the particles from the first to the second image.";
//        final String message2 = "Review the images selected in \"select an image pair\" and consider whether the images will result in a useful PIV output.";
//        final String message3 = "Compute PIV computes the velocity field between the first and second image from \"Select An Image Pair\" according to the parameters in \"Input PIV Parameters\". For more information see: ";
//
//        final PIVBasics3 pivBasics3 = new PIVBasics3(); // Interrogation Region or Window Size
//        final PIVBasicsLayout pivBasicsLayout = new PIVBasicsLayout();

        // listeners: when a lightbulb is clicked, disable all other buttons.
//        lightbulb1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                popupWindow(title1, message1, "", null, false, R.layout.popup_window_no_link, enabledCheck(allButtons));
//            }
//        });
//        lightbulb2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                popupWindow(title2, message2, "Learn More", pivBasics3, true, R.layout.popup_window_with_link, enabledCheck(allButtons));
//            }
//        });
//        lightbulb3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                popupWindow(title3, message3, "Learn More", pivBasicsLayout, true, R.layout.popup_window_with_link, enabledCheck(allButtons));
//            }
//        });
//    }

    public void reviewFile(View view) {
        reviewImageFromUrl();
        review.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    private void reviewImageFromUrl() {
        String[] urls = new String[2];
        urls[0] = frame1File.getAbsolutePath();
        urls[1] = frame2File.getAbsolutePath();

        Intent intent = new Intent(this, ViewPagerActivity.class).putExtra("string-array-urls", urls);
        startActivity(intent);
    }

    public void inputPivOptions(View view) {
        final PivOptionsPopup parameterPopup = new PivOptionsPopup(ImageActivity.this);

        // create listener for piv parameter save button
        View.OnClickListener saveListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pivParameters = parameterPopup.parameters;

                compute.setEnabled(true);
                parameters.setBackgroundColor(Color.parseColor("#00CC00"));

                parameterPopup.dismiss();
            }
        };

        parameterPopup.setSaveListener(saveListener);
        parameterPopup.show();
    }

    public void displayFile(View view) {
        Intent displayIntent = new Intent(this, ViewResultsActivity.class);
        displayIntent.putExtra(PivResultData.REPLACED, pivParameters.isReplace());
        displayIntent.putExtra(PivResultData.CORRELATION, (Serializable) resultData.getPivCorrelation());
        displayIntent.putExtra(PivResultData.INTERR_CENTERS, (Serializable) resultData.getInterrCenters());
        displayIntent.putExtra(PivResultData.VORTICITY, resultData.getVorticityValues());
        displayIntent.putExtra(PivResultData.MULTI, (Serializable) resultData.getPivCorrelationMulti());
        displayIntent.putExtra(PivResultData.ROWS, resultData.getRows());
        displayIntent.putExtra(PivResultData.COLS, resultData.getCols());
        displayIntent.putExtra(PivResultData.USERNAME, userName);

        if (pivParameters.isReplace()) {
            displayIntent.putExtra(PivResultData.REPLACE2, (Serializable) resultData.getPivReplaceMissing2());
        }

        startActivity(displayIntent);
        pickImageMultiple.setBackgroundColor(Color.parseColor("#243EDF"));
        compute.setBackgroundColor(Color.parseColor("#243EDF"));
        review.setBackgroundColor(Color.parseColor("#243EDF"));
        parameters.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    // Process Images
    public void processFile(View view) {
        PivRunner pivRunner = new PivRunner(ImageActivity.this, userName, pivParameters, frame1File, frame2File);
        resultData = pivRunner.Run();
        display.setEnabled(true);
        compute.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
}
