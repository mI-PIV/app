package com.onrpiv.uploadmedia.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.onrpiv.uploadmedia.Experiment.Popups.PivOptionsPopup;
import com.onrpiv.uploadmedia.R;

/**
 * The LightBulb is an image button that is placed on top of, and to the right of, the constructor-passed
 * view. When clicked, the LightBulb will display a pop-up with a title and message. There are two
 * types of pop-ups: 1. Just the title, message, and a close button. 2. Title, message, close button,
 * and a linked activity button. The type of pop-up is selected by the overloaded call to
 * 'setLightBulbOnClick'.
 */
@SuppressLint("ViewConstructor")
public class LightBulb extends AppCompatImageButton {
    public final View baseView;
    private final Context context;
    private final ViewGroup baseParent;
    private final int lightBulbWidth = 45;
    private final int lightBulbHeight = 45;


    /**
     * Add a light bulb to a widget view.
     * @param context The activity context.
     * @param baseView Add a light bulb to this View (widget).
     */
    public LightBulb(Context context, View baseView) {
        super(context);
        this.baseView = baseView;
        baseParent = (ViewGroup) baseView.getParent();
        this.context = context;

        // init our lightbulb
        setBackgroundResource(R.drawable.lightbulb);
        baseParent.addView(this);
        setVisibility(baseView.getVisibility());

        // lightbulb button layout
        setBulbLayout(lightBulbWidth, lightBulbHeight);

    }

    /**
     * Set the layout of the light bulb.
     * @param dpWidth The desired width in dp.
     * @param dpHeight The desired height in dp.
     */
    public void setBulbLayout(int dpWidth, int dpHeight) {
        // table row doesn't have the align_right and align_top parameters
        if (baseParent instanceof TableRow) {
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    dpToPixels(dpWidth),
                    dpToPixels(dpHeight)
            );
            setLayoutParams(params);
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    dpToPixels(dpWidth),
                    dpToPixels(dpHeight));
            params.addRule(RelativeLayout.ALIGN_RIGHT, baseView.getId());
            params.addRule(RelativeLayout.ALIGN_TOP, baseView.getId());
            setLayoutParams(params);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(dpToPixels(50));
        }
        requestLayout();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        baseView.setVisibility(visibility);
    }

    /**
     * Set the popup title and message when the lightbulb is clicked. A navigation button is created
     * using the linkedClass parameter and linkText.
     * @param title popup title
     * @param message popup message
     * @param linkedClass The navigation button directs to this class
     * @param linkText The navigation button text
     * @return
     */
    public LightBulb setLightBulbOnClick(final String title, final String message,
                                         final Object linkedClass, final String linkText,
                                         Window activityWindow){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.popup_window_with_link, null);

        Button navigateButton = (Button) customView.findViewById(R.id.button_navigate);
        navigateButton.setText(linkText);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // allow a new task from outside of the activity
                Intent linkedIntent = new Intent(context, linkedClass.getClass());
                linkedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // start the new activity
                context.startActivity(linkedIntent);
            }
        });

        popup(customView, title, message, activityWindow);
        return this;
    }

    /**
     * Set the popup title and message when the light bulb is clicked. Only a 'close' button is created.
     * @param title popup title
     * @param message popup message
     */
    public LightBulb setLightBulbOnClick(final String title, final String message, final Window activityWindow) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.popup_window_no_link, null);

        popup(customView, title, message, activityWindow);
        return this;
    }

    private void popup(final View customView, final String title, final String message, final Window activityWindow) {
//        final View parent = this;
        this.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Disable all activity interactions
                activityWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                activityWindow.getDecorView().setBackgroundColor(Color.GRAY);

                // set title and message
                TextView windowTitle = (TextView) customView.findViewById(R.id.popupWindowTitle);
                windowTitle.setText(title);

                TextView windowMessage = (TextView) customView.findViewById(R.id.popupWindowMessage);
                windowMessage.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                windowMessage.setText(message);

                // New instance of popup window
                final PopupWindow popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Setting an elevation value for popup window, it requires API level 21
                if (Build.VERSION.SDK_INT >= 21) {
                    popupWindow.setElevation(5.0f);
                }

                Button closeButton = (Button) customView.findViewById(R.id.button_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        activityWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        activityWindow.getDecorView().setBackgroundColor(Color.WHITE);
                    }
                });

                popupWindow.showAtLocation(baseParent, Gravity.CENTER, 0, 0);
            }
        });
    }

    private int dpToPixels(int dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics());
    }
}
