package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.collection.ArrayMap;

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
    private final TextView nMaxUpperText;
    private final TextView nMaxLowerText;
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

    public PivOptionsPopup(Context context) {
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
        nMaxUpperText = findViewById(R.id.nMaxUpper);
        TextView nMaxUpper_text = (TextView) findViewById(R.id.nMaxUpper_text);
        nMaxLowerText = findViewById(R.id.nMaxLower);
        TextView nMaxLower_text = (TextView) findViewById(R.id.nMaxLower_text);
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

        // keep advanced views in list for easy iteration
        hiddenViewList = new ArrayList<View>(
                Arrays.asList(
                        dtText, dt_text, nMaxUpperText, e_text, groupradio_text, radioGroup,
                        nMaxUpper_text, nMaxLowerText, nMaxLower_text, qMinText, qMin_text, EText
                )
        );

        // keep all textviews in list for easy iteration
        allTextViewList = new ArrayList<TextView>(
                Arrays.asList(
                        windowSizeText, overlapText, dtText, nMaxUpperText, nMaxLowerText, EText,
                        qMinText
                )
        );

        // set default texts
        setEditTextPIV.setText("Please Input the parameters to be used in your PIV experiment");
        setEditTextPIV.setTextSize(20);
        windowSizeText.setText("64");
        overlapText.setText("32");
        dtText.setText("1");
        nMaxUpperText.setText("25");
        nMaxLowerText.setText("5");
        qMinText.setText("1");
        EText.setText("2");
        radioGroup.check(R.id.yesRadio);
        savePIVDataButton.setEnabled(true);

        // load our ids to keys translation dictionary
        loadIdToKey();

        // set all listeners except save
        setListeners();
    }

    public void setSaveListener(View.OnClickListener saveListener) {
        savePIVDataButton.setOnClickListener(saveListener);
    }

    public void setFPSParameters(int fps, int frame1Num, int frame2Num) {
        float dt = calculateTimeDelta(fps, frame1Num, frame2Num);
        parameters.setDt(dt);
        dtText.setText(String.valueOf(dt));
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
        idToKey.put(nMaxUpperText.getId(), PivParameters.NUM_MAX_UPPER_KEY);
        idToKey.put(nMaxLowerText.getId(), PivParameters.NUM_MAX_LOWER_KEY);
        idToKey.put(EText.getId(), PivParameters.E_KEY);
        idToKey.put(qMinText.getId(), PivParameters.QMIN_KEY);
    }

    private float calculateTimeDelta(int fps, int frame1Num, int frame2Num) {
        int deltaFrameNums = frame2Num - frame1Num;
        return (float) deltaFrameNums/ (float) fps;
    }
}
