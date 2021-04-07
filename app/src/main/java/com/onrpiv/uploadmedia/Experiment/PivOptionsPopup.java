package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.collection.ArrayMap;

import com.onrpiv.uploadmedia.Learn.PIVBasics2;
import com.onrpiv.uploadmedia.Learn.PIVBasics3;
import com.onrpiv.uploadmedia.Learn.PIVBasics4;
import com.onrpiv.uploadmedia.Learn.PIVBasics5;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.BoolIntStructure;
import com.onrpiv.uploadmedia.Utilities.UserInputUtils;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;

import java.util.ArrayList;
import java.util.Arrays;


public class PivOptionsPopup extends AlertDialog {
    private final TextView windowSizeText;
    private final TextView overlapText;
    private final TextView dtText;
    //private final TextView nMaxUpperText;
    //private final TextView nMaxLowerText;
    private final TextView qMinText;
    private final TextView EText;
    private final Button savePIVDataButton;
    private final RadioGroup radioGroup;
    private final Button cancelPIVDataButton;
    private final CheckBox advancedCheckbox;

    public PivParameters parameters;
    private final ArrayList<View> hiddenViewList;
    private final ArrayList<TextView> allTextViewList;
    private final ArrayMap<Integer, String> idToKey;

    // tooltips variable
    private PopupWindow popupWindow;

    public PivOptionsPopup(final Context context) {
        super(context);

        parameters = new PivParameters();
        idToKey = new ArrayMap<>();

        // build dialog
        setTitle("PIV Parameters Dialog");
        setCancelable(false);
        setView(getLayoutInflater().inflate(R.layout.popup_piv_dialog, null));
        create();

        // init texts
        TextView setEditTextPIV = (TextView) findViewById(R.id.piv_options_description);
        windowSizeText = (EditText) findViewById(R.id.windowSize);
        overlapText = (EditText) findViewById(R.id.overlap);
        dtText = (EditText) findViewById(R.id.dt);
        TextView dt_text = (TextView) findViewById(R.id.dt_text);
        //nMaxUpperText = findViewById(R.id.nMaxUpper);
        //TextView nMaxUpper_text = (TextView) findViewById(R.id.nMaxUpper_text);
        //nMaxLowerText = findViewById(R.id.nMaxLower);
        //TextView nMaxLower_text = (TextView) findViewById(R.id.nMaxLower_text);
        qMinText = findViewById(R.id.qMin);
        TextView qMin_text = (TextView) findViewById(R.id.qMin_text);
        EText = findViewById(R.id.E);
        TextView e_text = (TextView) findViewById(R.id.E_text);
        TextView groupradio_text = (TextView) findViewById(R.id.groupradio_text);
        radioGroup = (RadioGroup) findViewById(R.id.groupradio);
        savePIVDataButton = findViewById(R.id.button_save_piv_data);
        cancelPIVDataButton = findViewById(R.id.button_cancel_piv_data);
        advancedCheckbox = findViewById(R.id.advancedCheckbox);
        advancedCheckbox.setChecked(false);
        Button lightbulb1 = findViewById(R.id.lightbulbInputDialog1);
        Button lightbulb2 = findViewById(R.id.lightbulbInputDialog2);
        Button lightbulb3 = findViewById(R.id.lightbulbInputDialog3);
        Button lightbulb4 = findViewById(R.id.lightbulbInputDialog4);
        Button lightbulb5 = findViewById(R.id.lightbulbInputDialog5);
        Button lightbulb6 = findViewById(R.id.lightbulbInputDialog6);

        // keep advanced views in list for easy iteration
        hiddenViewList = new ArrayList<View>(
//                Arrays.asList(
//                        dtText, dt_text, nMaxUpperText, e_text, groupradio_text, radioGroup,
//                        nMaxUpper_text, nMaxLowerText, nMaxLower_text, qMinText, qMin_text, EText,
//                        lightbulb3, lightbulb4
//                )
                // got rid of nMaxUpperText, nMaxLowerText, nMaxUpper_text and nMaxLower_text
                Arrays.asList(
                        dtText, dt_text, e_text, groupradio_text, radioGroup, qMinText, qMin_text,
                        EText, lightbulb3, lightbulb4, lightbulb5, lightbulb6
                )
        );

        // keep all textviews in list for easy iteration
        allTextViewList = new ArrayList<TextView>(
//                Arrays.asList(
//                        windowSizeText, overlapText, dtText, nMaxUpperText, nMaxLowerText, EText,
//                        qMinText, lightbulb3, lightbulb4
//                )
                // got rid of nMaxUpperText and nMaxLowerText
                Arrays.asList(
                        windowSizeText, overlapText, dtText, EText,
                        qMinText
                )
        );

        // set default texts
        setEditTextPIV.setText("Please Input the parameters to be used in your PIV experiment");
        setEditTextPIV.setTextSize(20);
        windowSizeText.setText("64");
        overlapText.setText("32");
        dtText.setText("1");
        //nMaxUpperText.setText("25");
        //nMaxLowerText.setText("5");
        qMinText.setText("1");
        EText.setText("2");
        radioGroup.check(R.id.yesRadio);
        savePIVDataButton.setEnabled(true);

        final String popupWindowTitle1 = "Window Size";
        final String popupWindowMessage1 = "Interrogation regions should contain at least five particles to result in a good correlation value.";
        final String linkText = "Learn More";
        final PIVBasics3 pivBasics3 = new PIVBasics3();
        final String popupWindowTitle2 = "Overlap";
        final String popupWindowMessage2 = "If the window size is 64, overlap should be 32.";
        final PIVBasics5 pivBasics5 = new PIVBasics5();
        final String popupWindowTitle3 = "Time Interval";
        final String popupWindowMessage3 = "The time between images. If you selected sequential images, this is 1/framerate.";
        final String popupWindowTitle4 = "Minimum Threshold";
        final String popupWindowMessage4 = "Set an initial Q value threshold of 1.3. Users can relax this standard by decreasing Q (minimum of one), or tighten this standard by increasing Q.";
        final PIVBasics2 pivBasics2 = new PIVBasics2();
        final String popupWindowTitle5 = "Median";
        final String popupWindowMessage5 = "Set a median threshold value of two. Increasing the median threshold value will result in a less stringent comparison and decreasing the median parameter will result in a more stringent comparison.";
        final PIVBasics4 pivBasics4 = new PIVBasics4();
        final String popupWindowTitle6 = "Replacing Missing Vectors";
        final String popupWindowMessage6 = "When would you choose yes vs no? \n\nYes: qualitative image analysis.\nNo: if you're using the vector data for further analysis.";

        RelativeLayout relativeLayout = findViewById(R.id.popupDialogInputRelativeLayout);

        popupWindow(lightbulb1, popupWindowTitle1, popupWindowMessage1, relativeLayout, context, pivBasics3, linkText, true, R.layout.popup_window_with_link);
        popupWindow(lightbulb2, popupWindowTitle2, popupWindowMessage2, relativeLayout, context, pivBasics5, linkText, true, R.layout.popup_window_with_link);
        popupWindow(lightbulb3, popupWindowTitle3, popupWindowMessage3, relativeLayout, context, null, "", false, R.layout.popup_window_no_link);
        popupWindow(lightbulb4, popupWindowTitle4, popupWindowMessage4, relativeLayout, context, pivBasics2, linkText, true, R.layout.popup_window_with_link);
        popupWindow(lightbulb5, popupWindowTitle5, popupWindowMessage5, relativeLayout, context, pivBasics4, linkText, true, R.layout.popup_window_with_link);
        popupWindow(lightbulb6, popupWindowTitle6, popupWindowMessage6, relativeLayout, context, null, "", false, R.layout.popup_window_no_link);

        // load our ids to keys translation dictionary
        loadIdToKey();

        // set all listeners except save
        setListeners();
    }

    public void setSaveListener(View.OnClickListener saveListener) {
        savePIVDataButton.setOnClickListener(saveListener);
    }

    private void setListeners() {
        // Hide/Show advanced options
        advancedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (View view : hiddenViewList) {
                    view.setVisibility(isChecked? View.VISIBLE : View.GONE);
                }
                savePIVDataButton.setEnabled(checkTexts());
            }
        });

        // Set the default overlap to 50% of windowSize if windowSize is changed from default
        windowSizeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                BoolIntStructure userInputCheckResult = UserInputUtils.checkUserInputInt(s.toString());
                if (s.length() != 0 && userInputCheckResult.getBool()) {
                    int half = (int) Math.round((double) userInputCheckResult.getInt() / 2);
                    overlapText.setText(String.valueOf(half));
                }

                savePIVDataButton.setEnabled(checkTexts());
            }
        });

        // Add the Listener to the RadioGroup
        radioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    // Check which radio button has been clicked
                    public void onCheckedChanged(RadioGroup group,
                                                 int checkedId)
                    {
                        boolean replaced = checkedId == R.id.yesRadio;
                        parameters.parameterDictionary.put(PivParameters.REPLACE_KEY, String.valueOf(replaced));
                        savePIVDataButton.setEnabled(checkTexts());
                    }
                });

        // cancel button
        cancelPIVDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        // add the text listener to all edittexts
        for (final TextView view : allTextViewList) {
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //EMPTY
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //EMPTY
                }

                @Override
                public void afterTextChanged(Editable s) {
                    BoolIntStructure userInput = UserInputUtils.checkUserInputInt(s.toString());
                    if (s.length() > 0 && userInput.getBool()) {
                        parameters.parameterDictionary.put(idToKey.get(view.getId()), String.valueOf(userInput.getInt()));
                        savePIVDataButton.setEnabled(checkTexts());
                    }
                }
            });
        }
    }

    private boolean checkTexts() {
        boolean basic = windowSizeText.getText().length() > 0 && overlapText.getText().length() > 0;
        boolean advanced = true;
        if (advancedCheckbox.isChecked()) {
            for (TextView view : allTextViewList) {
                boolean hasText = view.getText().length() > 0;
                advanced = advanced && hasText;
            }
        }

        return basic && advanced;
    }

    private void loadIdToKey() {
        idToKey.put(windowSizeText.getId(), PivParameters.WINDOW_SIZE_KEY);
        idToKey.put(overlapText.getId(), PivParameters.OVERLAP_KEY);
        idToKey.put(dtText.getId(), PivParameters.DT_KEY);
        //idToKey.put(nMaxUpperText.getId(), PivParameters.NUM_MAX_UPPER_KEY);
        //idToKey.put(nMaxLowerText.getId(), PivParameters.NUM_MAX_LOWER_KEY);
        idToKey.put(EText.getId(), PivParameters.E_KEY);
        idToKey.put(qMinText.getId(), PivParameters.QMIN_KEY);
    }

    private void popupWindow(final Button button, final String title, final String message, final RelativeLayout relativeLayout, final Context context, final Object linkedClass, final String linkText, final boolean hasLink, final int xml) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                button.setEnabled(false);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                final View customView = inflater.inflate(xml, null);

                TextView windowTitle = (TextView) customView.findViewById(R.id.popupWindowTitle);
                windowTitle.setText(title);

                TextView windowMessage = (TextView) customView.findViewById(R.id.popupWindowMessage);
                windowMessage.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                windowMessage.setText(message);

                // New instance of popup window
                popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Setting an elevation value for popup window, it requires API level 21
                if (Build.VERSION.SDK_INT >= 21) {
                    popupWindow.setElevation(5.0f);
                }

                if (hasLink) {
                    Button navigateButton = (Button) customView.findViewById(R.id.button_navigate);
                    navigateButton.setText(linkText);
//                    navigateButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            startActivity(new Intent(context.this, linkedClass.getClass()));
//                        }
//                    });
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
