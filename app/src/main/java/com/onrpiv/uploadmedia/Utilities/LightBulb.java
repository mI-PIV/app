package com.onrpiv.uploadmedia.Utilities;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;


public class LightBulb extends LinearLayout {
    private final View baseView;
    private ImageButton lightbulbButton;

    public LightBulb(Context context) {
        super(context);
        baseView = new View(context);
        Init(context);
    }

    public LightBulb(Context context, Button baseView) {
        super(context);
        this.baseView = baseView;
        Init(context);
    }

    private void Init(Context context) {
        // init lightbulb button
        lightbulbButton = new ImageButton(context);
        lightbulbButton.setBackgroundResource(R.drawable.lightbulb);

        // this linearlayout
        setOrientation(HORIZONTAL);
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(baseView, 0);
        addView(lightbulbButton, 1);

        // base view layout
        baseView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // lightbulb button layout
        LayoutParams params = new LinearLayout.LayoutParams(
                getPixelsFromDP(context, 45),
                getPixelsFromDP(context, 45),
                1);
            //overlap our basebutton
        params.setMarginStart(-100);
            //make sure the lightbulb is on top of basebutton
        if (Build.VERSION.SDK_INT >= 21) {
            lightbulbButton.setElevation(getPixelsFromDP(context, 2));
        }
        lightbulbButton.setLayoutParams(params);
    }

    public void setLightbulbOnClick(final Context context, final String title, final String message,
                                    final Object linkedClass, final String linkText){
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

        popup(customView, this, title, message);
    }

    public void setLightbulbOnClick(final Context context, final String title, final String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.popup_window_no_link, null);

        popup(customView, this, title, message);
    }

    private void popup(final View customView, final View parent, final String title, final String message) {
        lightbulbButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                lightbulbButton.setEnabled(false);

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
                        lightbulbButton.setEnabled(true);
                    }
                });

                popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
            }
        });
    }

    private int getPixelsFromDP(Context context, int dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics());
    }
}
