package com.onrpiv.uploadmedia.Experiment;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ImageFileAnimator;
import com.onrpiv.uploadmedia.Utilities.LightBulb;

import java.io.File;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class CheckCorrelationActivity extends AppCompatActivity {
    Button startAnimation, stopAnimation;
    ImageView imageAnim;
    File[] frames;
    AnimationDrawable animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startAnimation = (Button) findViewById(R.id.startAnim);
        stopAnimation = (Button) findViewById(R.id.stopAnim);
        stopAnimation.setEnabled(false);

        frames = (File[]) getIntent().getExtras().get("frames");

        new LightBulb(this, startAnimation).setLightBulbOnClick("Start Animation",
                "View the animation and consider if you can tell where the particles " +
                        "move between the first and second frame. If you can't correlate the " +
                        "images with your eyes, the PIV algorithm is less likely to be able to " +
                        "do so.", getWindow());

        imageAnim =  (ImageView) findViewById(R.id.reviewView);
        animation = ImageFileAnimator.getAnimator(getResources(), frames);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startAnimation(View view) {
        startAnimation.setEnabled(false);
        stopAnimation.setEnabled(true);

        animation.start();
        imageAnim.setImageDrawable(animation);
    }

    public void stopAnimation(View view) {
        stopAnimation.setEnabled(false);
        startAnimation.setEnabled(true);

        view.clearAnimation();
        animation.stop();
    }

    // ensuring the screen is locked to vertical position
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
