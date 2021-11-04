package com.onrpiv.uploadmedia.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import com.onrpiv.uploadmedia.Experiment.ImageActivity;
import com.onrpiv.uploadmedia.R;

import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_MULTI;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_REPLACED;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_SINGLE;


public class ParticleDensityPopup {

    private final AlertDialog popup;
    private LinearLayout densityReviewLayout;

    private final View.OnClickListener reviewDensityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popup.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    };

    public ParticleDensityPopup(Activity activity, Context context) {

        popup = createPopup(context);
    }

    public void showConfigPopup() {
        popup.show();
        popup.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private AlertDialog createPopup(Context context) {

        // creating the popup
        LayoutInflater inflater = LayoutInflater.from(context);
        View popup = inflater.inflate(R.layout.popup_review_dialog, null);

        // creating the layout of the popup
        LinearLayout densityLayout = popup.findViewById(R.id.densityRootLayout);
        //densityReviewLayout = popup.findViewById(R.id.popup_cam_res);

        // adding the buttons to the Layout
        densityLayout.addView(createRadioButton(context, "32x32", reviewDensityListener));
        densityLayout.addView(createRadioButton(context, "64x64", reviewDensityListener));
        densityLayout.addView(createRadioButton(context, "128x128", reviewDensityListener));

//        RadioGroup densityRadioGroup = findViewById(R.id.review_density_rgroup);
//        densityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                String value;
//                switch (checkedId) {
//                    case R.id.size32x32:
//                        // image for 32x32
//                        break;
//                    case R.id.size64x64:
//                        // image for 64x64
//                        break;
//                    default:
//                        // image for 128x128
//                }
////                settings.setVecOption(value);
////                applyButton.setEnabled(true);
//            }
//        });

        // build the popup
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setView(popup)
                .setMessage("Do you see at least 5 particles in the image?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // need to add backend here somehow
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // need to somehow add another popup with the information below
//                        new AlertDialog.Builder(ParticleDensityPopup.this)
//                                .setMessage("Please select frames with more particle density.")
//                                .setPositiveButton("Okay", null)
//                                .show();
                    }
                });

        return alertDialog.create();
    }

    private RadioButton createRadioButton(Context context, String text, View.OnClickListener listener) {
        RadioButton button = new RadioButton(context);
        button.setText(text);

        // layout
        button.setGravity(Gravity.CENTER | Gravity.START);
        button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        button.setLayoutDirection(View.LAYOUT_DIRECTION_INHERIT);
        button.setPadding(
                button.getPaddingLeft(),
                button.getPaddingTop(),
                50,
                button.getPaddingBottom());

        // logic
        button.setOnClickListener(listener);

        return button;
    }
}
