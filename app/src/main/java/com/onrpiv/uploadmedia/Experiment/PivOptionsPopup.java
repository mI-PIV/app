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

import com.onrpiv.uploadmedia.Learn.PIVBasics2;
import com.onrpiv.uploadmedia.Learn.PIVBasics3;
import com.onrpiv.uploadmedia.Learn.PIVBasics4;
import com.onrpiv.uploadmedia.Learn.PIVBasics5;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.BoolIntStructure;
import com.onrpiv.uploadmedia.Utilities.LightBulb;
import com.onrpiv.uploadmedia.Utilities.UserInputUtils;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;

import java.util.ArrayList;
import java.util.Arrays;


public class PivOptionsPopup extends AlertDialog {
    private final LightBulb windowSizeText;
    private final LightBulb overlapText;
    private final LightBulb dtText;
    private final LightBulb qMinText;
    private final LightBulb EText;
    private final LightBulb radioGroup;
    private final Button savePIVDataButton;
    private final Button cancelPIVDataButton;
    private final CheckBox advancedCheckbox;

    public PivParameters parameters;
    private final ArrayList<View> hiddenViewList;
    private final ArrayList<TextView> allTextViewList;
    private final ArrayMap<Integer, String> idToKey;


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

        EditText windowSizeEditText = (EditText) findViewById(R.id.windowSize);
        windowSizeText = new LightBulb(context, windowSizeEditText);

        EditText overlapEditText = (EditText) findViewById(R.id.overlap);
        overlapText = new LightBulb(context, overlapEditText);

        EditText dtEditText = (EditText) findViewById(R.id.dt);
        dtText = new LightBulb(context, dtEditText);
        TextView dt_text = (TextView) findViewById(R.id.dt_text);

        EditText qMinEditText = findViewById(R.id.qMin);
        qMinText = new LightBulb(context, qMinEditText);
        TextView qMin_text = (TextView) findViewById(R.id.qMin_text);

        EditText EEditText = findViewById(R.id.E);
        EText = new LightBulb(context, EEditText);
        TextView e_text = (TextView) findViewById(R.id.E_text);

        TextView groupradio_text = (TextView) findViewById(R.id.groupradio_text);
        RadioGroup rGroup = (RadioGroup) findViewById(R.id.groupradio);
        radioGroup = new LightBulb(context, rGroup);

        savePIVDataButton = findViewById(R.id.button_save_piv_data);
        cancelPIVDataButton = findViewById(R.id.button_cancel_piv_data);
        advancedCheckbox = findViewById(R.id.advancedCheckbox);
        advancedCheckbox.setChecked(false);

        // keep advanced views in list for easy iteration
        hiddenViewList = new ArrayList<View>(
                Arrays.asList(
                        dtText, dt_text, e_text, groupradio_text, radioGroup, qMinText, qMin_text,
                        EText
                )
        );

        // keep all textviews in list for easy iteration
        allTextViewList = new ArrayList<TextView>(
                Arrays.asList(
                        (TextView)windowSizeText.baseView, (TextView)overlapText.baseView,
                        (TextView)dtText.baseView, (TextView)EText.baseView,
                        (TextView)qMinText.baseView
                )
        );

        // set default texts
        setEditTextPIV.setText("Please Input the parameters to be used in your PIV experiment");
        setEditTextPIV.setTextSize(20);
        ((TextView) windowSizeText.baseView).setText("64");
        ((TextView) overlapText.baseView).setText("32");
        ((TextView) dtText.baseView).setText("1");
        ((TextView) qMinText.baseView).setText("1");
        ((TextView) EText.baseView).setText("2");
        ((RadioGroup) radioGroup.baseView).check(R.id.yesRadio);
        savePIVDataButton.setEnabled(true);

        final String linkText = "Learn More";

        windowSizeText.setLightBulbOnClick("Window Size",
                "Interrogation regions should contain at least five particles to result in a good correlation value.",
                new PIVBasics3(), linkText, getWindow());

        overlapText.setLightBulbOnClick("Overlap",
                "Normally the overlap is set at 50% of the window size.",
                new PIVBasics5(), linkText, getWindow());

        dtText.setLightBulbOnClick("Time Interval",
                "The time between images. If you selected sequential images, this is 1/framerate",
                getWindow());

        qMinText.setLightBulbOnClick("Minimum Threshold",
                "Set an initial Q value threshold of 1.3. Users can relax this standard by decreasing Q (minimum of one), or tighten this standard by increasing Q.",
                new PIVBasics2(), linkText, getWindow());

        EText.setLightBulbOnClick("Median",
                "Set a median threshold value of two. Increasing the median threshold value will result in a less stringent comparison and decreasing the median parameter will result in a more stringent comparison.",
                new PIVBasics4(), linkText, getWindow());

        radioGroup.setLightBulbOnClick("Replace Missing Vectors",
                "When would you choose yes vs no? \n\nYes: qualitative image analysis.\nNo: if you're using the vector data for further analysis.",
                getWindow());
        // TODO some gui work needs to be done on this bulb
//        radioGroup.setBulbMarginStart(-45);


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

        // TODO: The line below causes a bug because dtText is of LightBulb type. Is there another
        //  way we can default it to .05?
        // dtText.setText(String.valueOf(dt));
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
        ((TextView)windowSizeText.baseView).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                BoolIntStructure userInputCheckResult = UserInputUtils.checkUserInputInt(s.toString());
                if (s.length() != 0 && userInputCheckResult.getBool()) {
                    int half = (int) Math.round((double) userInputCheckResult.getInt() / 2);
                    ((TextView)overlapText.baseView).setText(String.valueOf(half));
                }

                savePIVDataButton.setEnabled(checkTexts());
            }
        });

        // Add the Listener to the RadioGroup
        ((RadioGroup)radioGroup.baseView).setOnCheckedChangeListener(
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
        boolean basic = ((TextView)windowSizeText.baseView).getText().length() > 0
                && ((TextView)overlapText.baseView).getText().length() > 0;
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
        idToKey.put(EText.getId(), PivParameters.E_KEY);
        idToKey.put(qMinText.getId(), PivParameters.QMIN_KEY);
    }

    private float calculateTimeDelta(int fps, int frame1Num, int frame2Num) {
        int deltaFrameNums = frame2Num - frame1Num;
        return (float) deltaFrameNums/ (float) fps;
    }
}
