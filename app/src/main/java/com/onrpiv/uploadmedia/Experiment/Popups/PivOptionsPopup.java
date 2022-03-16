package com.onrpiv.uploadmedia.Experiment.Popups;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.onrpiv.uploadmedia.Utilities.UserInput.BoolDoubleStruct;
import com.onrpiv.uploadmedia.Utilities.UserInput.BoolIntStructure;
import com.onrpiv.uploadmedia.Utilities.UserInput.UserInputUtils;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class PivOptionsPopup extends AlertDialog {
    private final EditText windowSizeText;
    private final EditText overlapText;
    private final EditText dtText;
    private final EditText qMinText;
    private final EditText EText;
    private final RadioGroup replaceRadioGroup;
    private final RadioGroup backSubRadioGroup;
    private final RadioGroup calibrationRadioGroup;
    private final RadioGroup corrMethodRadioGroup;
    private final Button savePIVDataButton;
    private final Button cancelPIVDataButton;
    private final CheckBox advancedCheckbox;

    public PivParameters parameters;
    private final ArrayList<View> hiddenViewList;
    private final ArrayList<TextView> doubleTextViewList;
    private final ArrayList<TextView> intTextViewList;
    private final ArrayMap<Integer, String> idToKey;


    public PivOptionsPopup(final Context context, String userName, String frameSetName, int frameOne, int frameTwo,
                           ActivityResultRegistry resultRegistry) {
        super(context);
        parameters = new PivParameters(frameSetName, frameOne, frameTwo);
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
        replaceRadioGroup = (RadioGroup) findViewById(R.id.params_replace_radiogroup);

        TextView bsRadioGroup_text = (TextView) findViewById(R.id.bs_radioText);
        backSubRadioGroup = (RadioGroup) findViewById(R.id.bs_radiogroup);

        TextView corrMethod_text = (TextView) findViewById(R.id.params_method_text);
        corrMethodRadioGroup = (RadioGroup) findViewById(R.id.params_method_group);

        TextView calibrationText = (TextView) findViewById(R.id.calibration_Text);
        calibrationRadioGroup = (RadioGroup) findViewById(R.id.calib_radio_group);
        RadioButton calibrationRadioBtn = (RadioButton) findViewById(R.id.calib_radio_calib);
        String calibString = CameraCalibrationResult.getSavedCalibrationPrettyPrint(context, userName);
        if (null == calibString) {
            calibrationRadioBtn.setVisibility(View.GONE);
        } else {
            calibrationRadioBtn.setText(calibString);
        }

        savePIVDataButton = findViewById(R.id.button_save_piv_data);
        cancelPIVDataButton = findViewById(R.id.button_cancel_piv_data);
        advancedCheckbox = findViewById(R.id.advancedCheckbox);
        advancedCheckbox.setChecked(false);

        // this needs to be place inside when the advanced parameters is clicked because we need the screen to be bigger for it.
        System.out.println("helooooooooooooooooooooooo");


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
                        calibrationText, calibrationRadioGroup, corrMethod_text, corrMethodRadioGroup
                )
        );

        // keep all input textviews in list for easy iteration
        doubleTextViewList = new ArrayList<TextView>(
                Arrays.asList(
                        dtText, EText, qMinText
                )
        );

        intTextViewList = new ArrayList<TextView>(
                Arrays.asList(
                        windowSizeText, overlapText
                )
        );

        // set default texts
        setEditTextPIV.setText("Please Input the parameters to be used in your PIV experiment");
        setEditTextPIV.setTextSize(20);
        windowSizeText.setText(Integer.toString(parameters.getWindowSize()));
        overlapText.setText(Integer.toString(parameters.getOverlap()));
        dtText.setText(Double.toString(1d/parameters.getDt()));
        qMinText.setText(Double.toString(parameters.getqMin()));
        EText.setText(Double.toString(parameters.getE()));
        replaceRadioGroup.check(R.id.params_replace_yes);
        savePIVDataButton.setEnabled(true);
        corrMethodRadioGroup.check(R.id.params_method_fft);

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
        dtText.setText(String.valueOf(fps));
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

                // need a larger and smaller window for when advanced parameters is checked
                if (isChecked) {
                    getWindow().setLayout(1450, 2450);
                }
                else {
                    getWindow().setLayout(1450, 1150);
                }
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
                        boolean replaced = checkedId == R.id.params_replace_yes;
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

        // Add listener for camera calibration radio group
        calibrationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.calib_radio_none:
                        parameters.setCameraCalibration(null);
                        break;
                    case R.id.calib_radio_calib:
                        CameraCalibrationResult calibrationResult =
                                CameraCalibrationResult.loadCalibration(context, userName);
                        parameters.setCameraCalibration(calibrationResult);
                }
                savePIVDataButton.setEnabled(checkTexts());
            }
        });

        // Add listener for correlation method radio group
        corrMethodRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                boolean fft = checkedId == R.id.params_method_fft;
                parameters.setFFT(fft);
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

        // add the text listener to all double edittexts
        for (final TextView view : doubleTextViewList) {
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
                    BoolDoubleStruct userInput = UserInputUtils.checkUserInputDouble(s.toString());
                    if (s.length() > 0 && userInput.getBool()) {
                        parameters.setValue(Objects.requireNonNull(idToKey.get(view.getId())), String.valueOf(userInput.getDouble()));
                        savePIVDataButton.setEnabled(checkTexts());
                    }
                }
            });
        }

        // add the text listener to all int edittexts
        for (final TextView view : intTextViewList) {
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

    private boolean checkTexts() {
        boolean basic = windowSizeText.getText().length() > 0
                && overlapText.getText().length() > 0;
        boolean advanced = true;
        if (advancedCheckbox.isChecked()) {
            for (TextView view : doubleTextViewList) {
                boolean hasText = view.getText().length() > 0;
                advanced = advanced && hasText;
            }

            for (TextView view : intTextViewList) {
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
