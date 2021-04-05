package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.github.chrisbanes.photoview.PhotoView;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.BoolIntStructure;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.Utilities.UserInputUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PivFrameSelectionPopup extends AlertDialog {

    private final EditText setNumText;
    private final EditText frame1Text;
    private final EditText frame2Text;
    private final Button saveButton;

    // tooltips variables
    private PopupWindow popupWindow;

    private final String userName;

    private File frameSetPath;
    public File frame1Path;
    public File frame2Path;

    private final int numberOfSets;
    private List<File> setFrames = new ArrayList<>();
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

        //init buttons
        TextView descriptionText = (TextView) findViewById(R.id.frame_selection_description);
        setNumText = (EditText) findViewById(R.id.imgSet);
        frame1Text = (EditText) findViewById(R.id.img1);
        frame2Text = (EditText) findViewById(R.id.img2);

        Button lightbulb1 = findViewById(R.id.lightbulbUserDialog1);
        Button lightbulb2 = findViewById(R.id.lightbulbUserDialog2);
        saveButton = findViewById(R.id.button_save_frame_selection);
        saveButton.setEnabled(false);

        Button cancelButton = findViewById(R.id.button_cancel_frame_selection);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        //init frame previews
        preview1 = (PhotoView) findViewById(R.id.frame_selection_preview1);
        preview2 = (PhotoView) findViewById(R.id.frame_selection_preview2);

        //get number of sets
        numberOfSets = PersistedData.getTotalFrameDirectories(context, userName);

        //description and framesets text
        descriptionText.setText("User '"+userName+"' has "+ numberOfSets +" image sets. The highest number set corresponds to the most recent generated frames.");
        descriptionText.setTextSize(15);
        if (numberOfSets < 1) {
            setNumText.setHint("No Frame Sets Found!");
        }else if (numberOfSets == 1) {
            setNumText.setHint("Set 1");
        } else {
            setNumText.setHint("Set 1 - "+ numberOfSets);
        }

        final String title1 = "Image Set";
        final String message1 = "The image set numbers are in order (time-wise) for each users' frame generation";
        final String title2 = "Images";
        final String message2 = "The PIV processing identifies the most likely displacements of each region of the image from the first image to the second image. For this reason, users should select images next to each other and in order (e.g., 1 & 2, or 5 & 6, etc.).";
        RelativeLayout relativeLayout = findViewById(R.id.popupDialogRelativeLayout);

        popupWindowListenerWithoutLink(lightbulb1, title1, message1, relativeLayout, context);
        popupWindowListenerWithoutLink(lightbulb2, title2, message2, relativeLayout, context);

        //set selection listeners
        setTextListeners();
    }

    public void setSaveListener(View.OnClickListener saveListener) {
        saveButton.setOnClickListener(saveListener);
    }

    private void setTextListeners() {
        setNumText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                setIsReady = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //EMPTY
            }

            @Override
            public void afterTextChanged(Editable s) {
                BoolIntStructure userInput = UserInputUtils.checkUserInputIntClamp(s.toString(), 1, numberOfSets);
                if (s.length() > 0 && userInput.getBool() && numberOfSets > 0) {
                    setIsReady = true;
                    frameSetPath = PathUtil.getFramesNumberedDirectory(userName, userInput.getInt());
                    setFrames = Arrays.asList(frameSetPath.listFiles());

                    setFrames.sort(null);
                    numFramesInSet = setFrames.size();

                    frame1Text.setText("", TextView.BufferType.EDITABLE);
                    frame1Text.setHint("Frame 1 - "+numFramesInSet);
                    frame2Text.setText("", TextView.BufferType.EDITABLE);
                    frame2Text.setHint("Frame 2 - "+numFramesInSet);
                }
            }
        });

        frame1Text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                frame1IsReady = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //EMPTY
            }

            @Override
            public void afterTextChanged(Editable s) {
                BoolIntStructure userInput = UserInputUtils.checkUserInputIntClamp(s.toString(),
                        1, numFramesInSet);

                if (s.length() > 0 && userInput.getBool() && setFrames.size() > 0) {
                    frame1IsReady = true;
                    userInput = checkFrameSelections(userInput);
                    int userInt = userInput.getInt();
                    frame2Text.setHint("Frame "+userInt+" - "+numFramesInSet);
                    frame1Path = setFrames.get(userInt-1).getAbsoluteFile();

                    preview1.setImageBitmap(BitmapFactory.decodeFile(frame1Path.getAbsolutePath()));

                    saveButton.setEnabled(checkAllSelections());
                }
            }
        });

        frame2Text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                frame2IsReady = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //EMPTY
            }

            @Override
            public void afterTextChanged(Editable s) {
                BoolIntStructure userInput = UserInputUtils.checkUserInputIntClamp(s.toString(),
                        1, numFramesInSet);

                if (s.length() > 0 && userInput.getBool() && setFrames.size() > 0) {
                    frame2IsReady = true;
                    userInput = checkFrameSelections(userInput);
                    int userInt = userInput.getInt();
                    frame2Path = setFrames.get(userInt-1).getAbsoluteFile();

                    preview2.setImageBitmap(BitmapFactory.decodeFile(frame2Path.getAbsolutePath()));

                    saveButton.setEnabled(checkAllSelections());
                }
            }
        });
    }

    private BoolIntStructure checkFrameSelections(BoolIntStructure input) {
        if (!frame1Text.hasSelection() || !frame2Text.hasSelection()) {
            return input;
        }

        int frame1 = Integer.parseInt(frame1Text.getText().toString());
        int frame2 = Integer.parseInt(frame2Text.getText().toString());
        int inputInt = input.getInt();

        //only check if they're the same; want to keep the possibility of reverse flow visualization
        if (frame1 == frame2) {
            if (inputInt == numFramesInSet) {
                input.setInt(inputInt-1);
            } else {
                input.setInt(inputInt+1);
            }
        }
        return input;
    }

    private boolean checkAllSelections() {
        return frame1IsReady && frame2IsReady && setIsReady;
    }

    private void popupWindowListenerWithoutLink(Button button, final String title, final String message, final RelativeLayout relativeLayout, final Context context) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                final View customView = inflater.inflate(R.layout.popup_window_no_link, null);

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

                Button closeButton = (Button) customView.findViewById(R.id.button_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
            }
        });
    }
}
