package com.onrpiv.uploadmedia.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

/**
 * The LightBulb class adds a light bulb to a widget (defaults to the right side) by extending a
 * LinearLayout and positioning the new linear layout to where the widget was in it's parent's
 * layout. Then the LightBulb class takes the widget as a child and creates a LightBulb image button.
 * If you want the LightBulb image button to have functionality, then you MUST set the onClick listener
 * using the 'SetLightBulbOnClick' method.
 */
@SuppressLint("ViewConstructor")
public class LightBulb extends LinearLayout {
    public final View baseView;
    private ImageButton lightbulbButton;
    private final Context context;

    // Here I'm assuming that every lightbulb has the same size and weight, however, we still have
    // setBulbLayout if we want that to change.
    private final int lightBulbWidth = 45;
    private final int lightBulbHeight = 45;
    private final float lightBulbWeight = 1;

    private Window activityWindow;

    // Goal: To have it so we just add a lightbulb button to a relativeLayout,
    // just like any other button. Then add a function called something like setPosition() that
    // takes a marginStart integer and a marginTop integer, this way we can place the button anywhere
    // in the RelativeLayout.
    // Possible issues:
    //      * Each activity that uses a button must call the function setPosition() which
    //      will be given "magic numbers" (numbers that are unique based on the lightbulbs position)
    //      * Each activity must have a RelativeLayout in the xml so that the lightbulb can be
    //      anywhere in the RelativeLayout.



    /**
     * Add a light bulb to a widget view.
     * @param context The activity context.
     * @param baseView Add a light bulb to this View (widget).
     */
    public LightBulb(Context context, View baseView) {
        super(context);
        this.baseView = baseView;
        ViewGroup baseViewParent = (ViewGroup) baseView.getParent();
        this.context = context;

        // init lightbulb button
        lightbulbButton = new ImageButton(context);
        lightbulbButton.setBackgroundResource(R.drawable.lightbulb);

        // swap places with base view
        baseViewParent.addView(this);
        baseViewParent.removeView(baseView);

        // this linearlayout
        setOrientation(HORIZONTAL);
        setLayoutParams(baseView.getLayoutParams());
        addView(baseView, 0);
        addView(lightbulbButton, 1);
        setVisibility(baseView.getVisibility());

        // base view layout
        baseView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // lightbulb button layout
        setBulbLayout(45, 45, 1, -45); //Negative values are required so the bulb overlaps the base view
    }

    /**
     * Set the layout of the light bulb.
     * @param dpWidth The desired width in dp.
     * @param dpHeight The desired height in dp.
     * @param weight The layout weight.
     * @param dpMarginStart The horizontal position in dp. A negative number should be used so the
     *                      light bulb overlaps the base view.
     */
    public void setBulbLayout(int dpWidth, int dpHeight, float weight, int dpMarginStart) {
        LayoutParams params = new LinearLayout.LayoutParams(
                getPixelsFromDP(dpWidth),
                getPixelsFromDP(dpHeight),
                weight);
        params.setMarginStart(getPixelsFromDP(dpMarginStart));
        params.topMargin = 100;

        if (Build.VERSION.SDK_INT >= 21) {
            lightbulbButton.setElevation(getPixelsFromDP(2));
        }
        lightbulbButton.setLayoutParams(params);
        requestLayout();
    }

    /** Sets the position of the light bulb in a RelativeLayout.
     * @param dpMarginStart The horizontal position in dp.
     * @param dpMarginTop The vertical position in dp.
     */
    public void setPosition(int dpMarginTop, int dpMarginStart) {
        LayoutParams params = new LinearLayout.LayoutParams(
                getPixelsFromDP(lightBulbWidth),
                getPixelsFromDP(lightBulbHeight),
                lightBulbWeight);

        // dpMarginStart+45-320 because the bulb is placed to the left of the View, which in this
        // case is a RelativeLayout. The user gives a dp that would act similar to one given from an
        // xml. (For example: if the user gave 50 dp, they'd expect the bulb to be place 50 dp to
        // to the right of the screen.) Problem: -320 doesn't move the bulb, it just stretches it, why?
        params.setMarginStart(getPixelsFromDP(dpMarginStart+45-320));
        params.topMargin = dpMarginTop;

        if (Build.VERSION.SDK_INT >= 21) {
            lightbulbButton.setElevation(getPixelsFromDP(2));
        }
        lightbulbButton.setLayoutParams(params);
        requestLayout();
    }

    /**
     * Set the light bulb horizontal position. A negative number should be used so the light bulb
     * overlaps the base view.
     * @param dpMarginStart The horizontal position in dp. A negative number should be used so the
     *                      light bulb overlaps the base view.
     */
    public void setBulbMarginStart(int dpMarginStart) {
        LayoutParams params = (LinearLayout.LayoutParams) lightbulbButton.getLayoutParams();
        params.setMarginStart(getPixelsFromDP(dpMarginStart));
        lightbulbButton.setLayoutParams(params);
        requestLayout();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        lightbulbButton.setVisibility(visibility);
        baseView.setVisibility(visibility);
    }

    /**
     * Set the popup title and message when the lightbulb is clicked. A navigation button is created
     * using the linkedClass parameter and linkText.
     * @param title popup title
     * @param message popup message
     * @param linkedClass The navigation button directs to this class
     * @param linkText The navigation button text
     */
    public void setLightBulbOnClick(final String title, final String message,
                                    final Object linkedClass, final String linkText,
                                    Window activityWindow){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.popup_window_with_link, null);

        Button navigateButton = (Button) customView.findViewById(R.id.button_navigate);
        navigateButton.setText(linkText);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, linkedClass.getClass()));
            }
        });

        popup(customView, title, message, activityWindow);
    }

    /**
     * Set the popup title and message when the light bulb is clicked. Only a 'close' button is created.
     * @param title popup title
     * @param message popup message
     */
    public void setLightBulbOnClick(final String title, final String message, final Window activityWindow) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.popup_window_no_link, null);

        popup(customView, title, message, activityWindow);
    }

    private void popup(final View customView, final String title, final String message, final Window activityWindow) {
        final View parent = this;
        lightbulbButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Disable all activity interactions
                activityWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

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
                    }
                });

                popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
            }
        });
    }

    private int getPixelsFromDP(int dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics());
    }
}
