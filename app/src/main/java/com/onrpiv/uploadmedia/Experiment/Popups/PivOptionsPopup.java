package com.onrpiv.uploadmedia.Experiment.Popups;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultRegistry;
import androidx.collection.ArrayMap;

import com.onrpiv.uploadmedia.Learn.PIVBasics2;
import com.onrpiv.uploadmedia.Learn.PIVBasics3;
import com.onrpiv.uploadmedia.Learn.PIVBasics4;
import com.onrpiv.uploadmedia.Learn.PIVBasics5;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.Calibration.CameraCalibrationResult;
import com.onrpiv.uploadmedia.Utilities.LightBulb;
import com.onrpiv.uploadmedia.Utilities.UserInput.BoolIntStructure;
import com.onrpiv.uploadmedia.Utilities.UserInput.UserInputUtils;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class PivOptionsPopup extends AlertDialog {
    private final EditText windowSizeText;
    private final EditText overlapText;
    private final EditText dtText;
    private final EditText qMinText;
    private final EditText EText;
    private final RadioGroup replaceRadioGroup;
    private final RadioGroup backSubRadioGroup;
    private final Spinner calibrationSpinner;
    private final Button savePIVDataButton;
    private final Button cancelPIVDataButton;
    private final CheckBox advancedCheckbox;

    public PivParameters parameters;
    private final ArrayList<View> hiddenViewList;
    private final ArrayList<TextView> allTextViewList;
    private final ArrayMap<Integer, String> idToKey;


    public PivOptionsPopup(final Context context, String userName, int frameSet, int frameOne, int frameTwo,
                           ActivityResultRegistry resultRegistry) {
        super(context);
        parameters = new PivParameters(frameSet, frameOne, frameTwo);
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
        qMinText = findViewById(R.id.qMin);
        TextView qMin_text = (TextView) findViewById(R.id.qMin_text);

        EText = findViewById(R.id.E);
        TextView e_text = (TextView) findViewById(R.id.E_text);

        TextView radioGroup_text = (TextView) findViewById(R.id.groupradio_text);
        replaceRadioGroup = (RadioGroup) findViewById(R.id.replace_radiogroup);
        TextView bsRadioGroup_text = (TextView) findViewById(R.id.bs_radioText);
        backSubRadioGroup = (RadioGroup) findViewById(R.id.bs_radiogroup);

        TextView calibrationText = (TextView) findViewById(R.id.calibration_Text);
        calibrationSpinner = findViewById(R.id.calibration_spinner);

        savePIVDataButton = findViewById(R.id.button_save_piv_data);
        cancelPIVDataButton = findViewById(R.id.button_cancel_piv_data);
        advancedCheckbox = findViewById(R.id.advancedCheckbox);
        advancedCheckbox.setChecked(false);

        // lightbulbs
        final String linkText = "Learn More";
        new LightBulb(context, windowSizeText).setLightBulbOnClick("Window Size",
                "Interrogation regions should contain at least five particles to result in a good correlation value.",
                new PIVBasics3(), linkText, getWindow());
        new LightBulb(context, overlapText).setLightBulbOnClick("Overlap",
                "Normally the overlap is set at 50% of the window size.",
                new PIVBasics5(), linkText, getWindow());
        LightBulb dtLB = new LightBulb(context, dtText).setLightBulbOnClick("Time Interval",
                "The time between images. If you selected sequential images, this is 1/framerate.",
                getWindow());
        LightBulb qMinLB = new LightBulb(context, qMinText).setLightBulbOnClick("Minimum Threshold",
                "Set an initial Q value threshold of 1.3. Users can relax this standard by decreasing Q (minimum of one), or tighten this standard by increasing Q.",
                new PIVBasics2(), linkText, getWindow());
        LightBulb eTextLB = new LightBulb(context, EText).setLightBulbOnClick("Median",
                "Set a median threshold value of two. Increasing the median threshold value will result in a less stringent comparison and decreasing the median parameter will result in a more stringent comparison.",
                new PIVBasics4(), linkText, getWindow());
        LightBulb radioGroupLB = new LightBulb(context, replaceRadioGroup).setLightBulbOnClick("Replace Missing Vectors",
                "When would you choose yes vs no? \n\nYes: qualitative image analysis.\nNo: if you're using the vector data for further analysis.",
                getWindow());

        // keep advanced views in list for easy iteration
        hiddenViewList = new ArrayList<View>(
                Arrays.asList(
                        dtText, dt_text, e_text, radioGroup_text, replaceRadioGroup, qMinText, qMin_text,
                        EText, dtLB, qMinLB, eTextLB, radioGroupLB, backSubRadioGroup, bsRadioGroup_text,
                        calibrationText, calibrationSpinner
                )
        );

        // keep all textviews in list for easy iteration
        allTextViewList = new ArrayList<TextView>(
                Arrays.asList(
                        windowSizeText, overlapText, dtText, EText, qMinText
                )
        );

        // set default texts
        setEditTextPIV.setText("Please Input the parameters to be used in your PIV experiment");
        setEditTextPIV.setTextSize(20);
        windowSizeText.setText(Integer.toString(parameters.getWindowSize()));
        overlapText.setText(Integer.toString(parameters.getOverlap()));
        dtText.setText(Double.toString(parameters.getDt()));
        qMinText.setText(Double.toString(parameters.getqMin()));
        EText.setText(Double.toString(parameters.getE()));
        replaceRadioGroup.check(R.id.yesRadio);
        savePIVDataButton.setEnabled(true);

        // load our ids to keys translation dictionary
        loadIdToKey();

        // set all listeners except save
        setListeners(context, userName, resultRegistry);
    }

    public void setSaveListener(View.OnClickListener saveListener) {
        savePIVDataButton.setOnClickListener(saveListener);
    }

    public void setFPSParameters(int fps, int frame1Num, int frame2Num) {
        float dt = calculateTimeDelta(fps, frame1Num, frame2Num);
        parameters.setDt(dt);
        dtText.setText(String.valueOf(dt));
    }

    private void setListeners(Context context, String userName, ActivityResultRegistry resultRegistry) {
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

        // Add the Listener to the replace RadioGroup
        replaceRadioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    // Check which radio button has been clicked
                    public void onCheckedChanged(RadioGroup group,
                                                 int checkedId)
                    {
                        boolean replaced = checkedId == R.id.yesRadio;
                        parameters.setReplace(replaced);
                        savePIVDataButton.setEnabled(checkTexts());
                    }
                });

        // Add listener for background subtract
        backSubRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.bs_frameset:
                        parameters.setBackgroundSelection(PivParameters.BACKGROUNDSUB_ALLFRAME);
                        break;
                    case R.id.bs_twoframe:
                        parameters.setBackgroundSelection(PivParameters.BACKGROUNDSUB_TWOFRAME);
                        break;
                    default:
                        parameters.setBackgroundSelection(PivParameters.BACKGROUNDSUB_NONE);
                        break;
                }
                savePIVDataButton.setEnabled(checkTexts());
            }
        });

        // Add listener and list adapter for camera calibration spinner
        ArrayMap<String, String> calibrationNamesMap =
                CameraCalibrationResult.getSavedCalibrationNamesMapping(context, userName);

        calibrationSpinner.setAdapter(createCalibrationSpinnerAdapter(context, calibrationNamesMap));
        calibrationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (selected.equals("None")) {
                    parameters.setCameraCalibration(null);
                } else {
                    // set the camera calibration in the parameters
                    CameraCalibrationResult calibrationResult =
                            CameraCalibrationResult.loadCalibrationByName(
                                    context, userName, calibrationNamesMap.get(selected));
                    parameters.setCameraCalibration(calibrationResult);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // EMPTY
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
                        parameters.setValue(Objects.requireNonNull(idToKey.get(view.getId())), String.valueOf(userInput.getInt()));
                        savePIVDataButton.setEnabled(checkTexts());
                    }
                }
            });
        }
    }

    private ArrayAdapter<String> createCalibrationSpinnerAdapter(Context context,
                                                                 ArrayMap<String, String> calibrationNamesMap) {

        List<String> calibrationSpinnerOptions = new ArrayList<>(calibrationNamesMap.keySet());
        calibrationSpinnerOptions.add("None");
        return new ArrayAdapter<>(context,
                R.layout.support_simple_spinner_dropdown_item, calibrationSpinnerOptions);
    }

    private boolean checkTexts() {
        boolean basic = windowSizeText.getText().length() > 0
                && overlapText.getText().length() > 0;
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
