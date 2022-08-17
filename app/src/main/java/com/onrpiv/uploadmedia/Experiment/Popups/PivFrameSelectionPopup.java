package com.onrpiv.uploadmedia.Experiment.Popups;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.github.chrisbanes.photoview.PhotoView;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.LightBulb;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.UserInput.BoolIntStructure;
import com.onrpiv.uploadmedia.Utilities.UserInput.UserInputUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PivFrameSelectionPopup extends AlertDialog {

    private final EditText frame1Text;
    private final TableRow secondFrameTableRow;
    private final RadioGroup frame2RadioGroup;
    private final Spinner frameSetSpinner;
    private final Button saveButton;
    private final SeekBar frame1Slider;
    private final HashMap<SeekBar, EditText> seekBarToTextDict;

    private final String userName;
    private final Context context;

    public File frameSetPath;
    public File frame1Path;
    public File frame2Path;
    public String frameSetName;
    public int frame1Num;
    public int frame2Num;

    public boolean multipleFrames = false;
    private static final String ALL = "All";

    private final List<File> setFrames = new ArrayList<>();
    final List<String> frameSetsList;
    private int numFramesInSet;

    private final PhotoView preview1;
    private final PhotoView preview2;

    private boolean setIsReady = false;
    private boolean frame1IsReady = false;
    private boolean frame2IsReady = false;

    public PivFrameSelectionPopup(@NonNull final Context context, String userName) {
        super(context);

        //set alert dialog stuff
        setTitle("Frame Selection");
        setCancelable(false);
        setView(getLayoutInflater().inflate(R.layout.popup_frame_selection, null));
        create();

        this.userName = userName;
        this.context = context;

        //init buttons
        TextView descriptionText = (TextView) findViewById(R.id.frame_selection_description);
        frameSetSpinner = findViewById(R.id.frameset_spinner);
        frame1Text = (EditText) findViewById(R.id.img1);

        secondFrameTableRow = findViewById(R.id.second_frame_tablerow);
        secondFrameTableRow.setVisibility(View.GONE);
        frame2RadioGroup = findViewById(R.id.second_radio_group);
        frame2RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int checkedId = radioGroup.getCheckedRadioButtonId();
                RadioButton selectedButton = findViewById(checkedId);
                String rawText = selectedButton.getText().toString();
                if (rawText.equals(ALL)) {
                    frame2Num = numFramesInSet + 1;  // zero-based idx to one-based idx
                } else {
                    int frameAddition = Integer.parseInt(rawText.substring(rawText.length() - 1));
                    frame2Num = frame1Num + frameAddition;
                }
                frame2IsReady = true;
                updatePreview(preview2, frame2Num);
                saveButton.setEnabled(checkAllSelections());
            }
        });

        CheckBox wholeSetCheckBox = findViewById(R.id.whole_set_checkbox);
        wholeSetCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                multipleFrames = isChecked;
                saveButton.setEnabled(checkAllSelections() || isChecked);

                if (isChecked) {
                    AlertDialog.Builder warning = new AlertDialog.Builder(context);
                    warning.setMessage("Processing a multiple frames might take a long time to process. " +
                            "Depending on the amount of frames selected.");
                    warning.setPositiveButton( "Okay", (OnClickListener) null);
                    warning.setNegativeButton("Nevermind", (dialog, which) -> wholeSetCheckBox.setChecked(false));
                    warning.create().show();

                    frame1Text.setText("1");
                }
            }
        });

        saveButton = findViewById(R.id.button_save_frame_selection);
        saveButton.setEnabled(false);

        Button cancelButton = findViewById(R.id.button_cancel_frame_selection);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        //init sliders
        frame1Slider = (SeekBar) findViewById(R.id.first_frame_slider);
        seekBarToTextDict = new HashMap<>();
        seekBarToTextDict.put(frame1Slider, frame1Text);

        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    EditText text = seekBarToTextDict.get(seekBar);
                    text.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // EMPTY
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //EMPTY
            }
        };

        frame1Slider.setOnSeekBarChangeListener(seekBarListener);
        frame1Slider.setMax(1);

        //init frame previews
        preview1 = (PhotoView) findViewById(R.id.frame_selection_preview1);
        preview2 = (PhotoView) findViewById(R.id.frame_selection_preview2);

        // gather all frame set names
        frameSetsList = PathUtil.getFrameSetNames(context, userName);
        frameSetsList.sort(null);
        // spinner adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, frameSetsList);
        assert frameSetSpinner != null;
        frameSetSpinner.setAdapter(spinnerAdapter);

        // lightbulbs
        new LightBulb(context, frameSetSpinner).setLightBulbOnClick("Image Set",
                "The image set numbers are in order (time-wise) for each users' frame generation",
                getWindow());
        new LightBulb(context, Objects.requireNonNull(frame1Text)).setLightBulbOnClick("Images",
                "The PIV processing identifies the most likely displacements of each region " +
                        "of the image from the first image to the second image. For this reason, " +
                        "users should select images next to each other and in order (e.g., 1 & 2, " +
                        "or 5 & 6, etc.).",
                getWindow());

        //set selection listeners
        setTextListeners();
    }

    public void setSaveListener(View.OnClickListener saveListener) {
        saveButton.setOnClickListener(saveListener);
    }

    private void setTextListeners() {
        frameSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setFrames.clear();
                frameSetName = frameSetsList.get(i);
                frameSetPath = PathUtil.getFramesNamedDirectory(context, userName, frameSetName);

                File[] frames = frameSetPath.listFiles();
                Arrays.sort(Objects.requireNonNull(frames));
                for (File frame : frames) {
                    if (!frame.getName().equals("BACKGROUND.jpg"))
                        setFrames.add(frame);
                }

                setFrames.sort(null);
                numFramesInSet = setFrames.size() - 1;  // last frame isn't an option for frame 1

                setIsReady = true;
                frame1Text.setText("", TextView.BufferType.EDITABLE);
                frame1Text.setHint("Frame 1 - " + numFramesInSet);
                frame1Slider.setMax(numFramesInSet);
                preview1.setImageResource(android.R.color.transparent);
                preview2.setImageResource(android.R.color.transparent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // EMPTY
            }
        });

        frame1Text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                frame1IsReady = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                frame2RadioGroup.removeAllViews();
                secondFrameTableRow.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                BoolIntStructure userInput = UserInputUtils.checkUserInputIntClamp(s.toString(),
                        1, numFramesInSet);

                if (s.length() > 0 && userInput.getBool() && setFrames.size() > 0) {
                    frame1IsReady = true;
                    userInput = checkFrameSelections(userInput);
                    int userInt = userInput.getInt();
                    frame1Slider.setProgress(userInt);
                    frame1Num = userInt;

                    updatePreview(preview1, frame1Num);

                    saveButton.setEnabled(checkAllSelections());
                    secondFrameTableRow.setVisibility(View.VISIBLE);
                    frame2RadioGroup.removeAllViews();
                    populateSecondFrameRadioButtons();
                }
            }
        });
    }

    private void populateSecondFrameRadioButtons() {
        // Check that the options don't go past number of frames
        List<Integer> fullOptions = new ArrayList<Integer>();
        fullOptions.add(1);
        fullOptions.add(2);
        fullOptions.add(3);
        fullOptions.add(4);
        fullOptions.removeIf(option -> frame1Num + option - 1 > numFramesInSet);

        // create radio buttons from options
        for (Integer option : fullOptions) {
            if (option == 4 && multipleFrames) {
                RadioButton allFrames = new RadioButton(context);
                allFrames.setText(ALL);
                frame2RadioGroup.addView(allFrames);
            } else {
                RadioButton newButton = new RadioButton(context);
                // ensure that the integer 'option' is always the last char
                newButton.setText("+" + option);
                frame2RadioGroup.addView(newButton);
                if (option == 1) {
                    frame2RadioGroup.check(newButton.getId());
                    frame2Num = frame1Num + 1;
                    updatePreview(preview2, frame2Num);
                }
            }
        }
    }

    private void updatePreview(PhotoView preview, int frameNum) {
        File framePath = setFrames.get(frameNum - 1).getAbsoluteFile();
        if (preview == preview1) {
            frame1Path = framePath;
        } else {
            frame2Path = framePath;
        }
        preview.setImageBitmap(BitmapFactory.decodeFile(framePath.getAbsolutePath()));
    }

    private BoolIntStructure checkFrameSelections(BoolIntStructure input) {
        if (!frame1Text.hasSelection()) {
            return input;
        }

        int frame1 = Integer.parseInt(frame1Text.getText().toString());
        int inputInt = input.getInt();

        //only check if they're the same; want to keep the possibility of reverse flow visualization
        if (frame1 == frame2Num) {
            if (inputInt == numFramesInSet) {
                input.setInt(inputInt - 1);
            } else {
                input.setInt(inputInt + 1);
            }
        }
        return input;
    }

    private boolean checkAllSelections() {
        return frame1IsReady && frame2IsReady && setIsReady;
    }
}

