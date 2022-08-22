package com.onrpiv.uploadmedia.Utilities;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;

public class ImageFileAnimator {
    public static AnimationDrawable getAnimator(Resources res, File[] frames) {
        AnimationDrawable animator = new AnimationDrawable();

        // load the image files into the animator
        for (File frameFile : frames) {
            animator.addFrame(new BitmapDrawable(
                    res,
                    BitmapFactory.decodeFile(frameFile.getAbsolutePath())),
                    500);
        }

        // add a transparent frame so it's obvious when the animation restarts
        Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
        animator.addFrame(transparentDrawable, 100);

        animator.setOneShot(false);
        return animator;
    }
}
