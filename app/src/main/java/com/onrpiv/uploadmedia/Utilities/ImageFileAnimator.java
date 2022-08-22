package com.onrpiv.uploadmedia.Utilities;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;

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

        animator.setOneShot(false);
        return animator;
    }
}
