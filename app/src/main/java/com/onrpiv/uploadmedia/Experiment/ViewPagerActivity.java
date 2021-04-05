package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.onrpiv.uploadmedia.R;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class ViewPagerActivity extends AppCompatActivity {
    Button startAnimation;
    String [] urls;
    AnimationDrawable animation;

    // tooltips variables
    Context context;
    private PopupWindow popupWindow;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_layout);
        startAnimation = (Button) findViewById(R.id.startAnim);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        urls = intent.getStringArrayExtra("string-array-urls");
        popupWindowReviewRun();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startAnimation(View view) {
        ImageView imageAnim =  (ImageView) findViewById(R.id.reviewView);
        animation = new AnimationDrawable();
        Drawable d1 = Drawable.createFromPath(urls[0]);
        Drawable d2 = Drawable.createFromPath(urls[1]);
        Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
        animation.addFrame(d1,500);
        animation.addFrame(d2,500);
        animation.addFrame(transparentDrawable, 1);
        animation.addFrame(d1,1);
        animation.setOneShot(false);
        if (animation.isRunning()) {
            imageAnim.clearAnimation();
            animation.stop();
            animation.start();
            imageAnim.setImageDrawable(animation);
        } else {
            animation.start();
            imageAnim.setImageDrawable(animation);
        }
    }

    public void stopAnimation(View view) {
        view.clearAnimation();
        animation.stop();
    }

    private void popupWindowReviewRun() {
        context = getApplicationContext();
        relativeLayout = (RelativeLayout) findViewById(R.id.popupReviewRelativeLayout);

        Button lightbulb1 = (Button) findViewById(R.id.lightbulbReviewLayout1);

        String title = "Start Animation";
        String message = "View the animation and consider if you can tell where the particles move between the first and second frame. If you can't correlate the images with your eyes, the PIV algorithm is less likely to be able to do so.";

        popupWindowNoLink(lightbulb1, title, message);
    }

    private void popupWindowNoLink(Button button, final String popUpWindowTitle, final String popupWindowMessage) {

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

                final View customView = inflater.inflate(R.layout.popup_window_no_link, null);

                TextView windowTitle = (TextView) customView.findViewById(R.id.popupWindowTitle);
                windowTitle.setText(popUpWindowTitle);

                TextView windowMessage = (TextView) customView.findViewById(R.id.popupWindowMessage);
                windowMessage.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                windowMessage.setText(popupWindowMessage);

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
