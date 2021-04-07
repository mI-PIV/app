package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.Learn.PIVBasics3;
import com.onrpiv.uploadmedia.Learn.PIVBasicsLayout;
import com.onrpiv.uploadmedia.R;
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
    private Uri fileUri;
    private String userName;
    private PivParameters pivParameters;
    private File frame1File;
    private File frame2File;
    private PivResultData resultData;


    // tooltips variables
    private PopupWindow popupWindow;
    private RelativeLayout relativeLayout;
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
        popupWindowImageLayoutRun();
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
    private void popupWindowImageLayoutRun()
    {
        context = getApplicationContext();
        relativeLayout = (RelativeLayout) findViewById(R.id.imageActivityRelativeLayout);

        Button lightbulb1 = (Button) findViewById(R.id.lightbulbImageLayout1);
        Button lightbulb2 = (Button) findViewById(R.id.lightbulbImagelayout2);
        Button lightbulb3 = (Button) findViewById(R.id.lightbulbImageLayout3);

        String title1 = "Image Pair";
        String title2 = "Image Correlation";
        String title3 = "Compute PIV";

        String message1 = "You need to select two images to compute movement of the particles from the first to the second image.";
        String message2 = "Review the images selected in \"select an image pair\" and consider whether the images will result in a useful PIV output.";
        String message3 = "Compute PIV computes the velocity field between the first and second image from \"Select An Image Pair\" according to the parameters in \"Input PIV Parameters\". For more information see: ";

        PIVBasics3 pivBasics3 = new PIVBasics3(); // Interrogation Region or Window Size
        PIVBasicsLayout pivBasicsLayout = new PIVBasicsLayout();

        popupWindow(lightbulb1, title1, message1, "", null, false, R.layout.popup_window_no_link);
        popupWindow(lightbulb2, title2, message2, "Window Size", pivBasics3, true, R.layout.popup_window_with_link);
        popupWindow(lightbulb3, title3, message3, "PIV Basics", pivBasicsLayout, true, R.layout.popup_window_with_link);
    }

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

    private void popupWindow(final Button button, final String popUpWindowTitle, final String popupWindowMessage, final String linkText, final Object myClass, final boolean hasLink, final int xml) {

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // when button is clicked, revert back to main layout. Put code for that here. Once
                // it's functional, there's no need for the button.setEnabled().

                button.setEnabled(false);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

                final View customView = inflater.inflate(xml, null);

                TextView windowTitle = (TextView) customView.findViewById(R.id.popupWindowTitle);
                windowTitle.setText(popUpWindowTitle);

                TextView windowMessage = (TextView) customView.findViewById(R.id.popupWindowMessage);
                windowMessage.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                windowMessage.setText(popupWindowMessage);

                // New instance of popup window
                popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Setting an elevation value for popup window, it requires API level 21
                if (Build.VERSION.SDK_INT >= 21) {
                    popupWindow.setElevation(5.0f);
                }

                if (hasLink) {
                    Button navigateButton = (Button) customView.findViewById(R.id.button_navigate);
                    navigateButton.setText(linkText);
                    navigateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(ImageActivity.this, myClass.getClass()));
                        }
                    });
                }

                Button closeButton = (Button) customView.findViewById(R.id.button_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        button.setEnabled(true);
                    }
                });

                popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
            }
        });
    }
}
