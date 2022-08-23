package com.onrpiv.uploadmedia.Experiment.Popups;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import com.github.chrisbanes.photoview.PhotoView;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;

public class DensityPreviewPopup {
    private int cropSize = 32;
    private AlertDialog popup;
    private View popupLayout;
    private String frame1Path;
    private String frame2Path;
    private final Context context;
    private DialogInterface.OnClickListener yesListener;
    private boolean isFake = false;


    public DensityPreviewPopup(Activity activity, PivFrameSelectionPopup frameSelection,
                               DialogInterface.OnClickListener yesListener) {
        this.context = activity;

        // whole set processing doesn't have density preview
        if (frameSelection.wholeSetProc) {
            AlertDialog fakeDialog = new AlertDialog.Builder(context).create();
            yesListener.onClick(fakeDialog, DialogInterface.BUTTON_POSITIVE);
            isFake = true;
        } else {
            // inflate our button radio group and get the crop size radio group
            popupLayout = activity.getLayoutInflater().inflate(R.layout.popup_review_dialog, null);

            RadioGroup cropSizeRadioGroup = popupLayout.findViewById(R.id.review_density_rgroup);
            cropSizeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.size64x64:
                            cropSize = 64;
                            break;
                        case R.id.size128x128:
                            cropSize = 128;
                            break;
                        default:
                            cropSize = 32;
                    }
                    buildView();
                    popup.setView(popupLayout);
                }
            });

            frame1Path = frameSelection.frame1Path.getAbsolutePath();
            frame2Path = frameSelection.frame2Path.getAbsolutePath();

            this.yesListener = yesListener;
            popup = buildPopup().create();
        }
    }

    public void show() {
        if (!isFake)
            popup.show();
    }

    private AlertDialog.Builder buildPopup() {
        buildView();
        return new AlertDialog.Builder(context)
                .setTitle("Particle Density")
                .setMessage(Html.fromHtml("Do you see movement of at least 5 particles " +
                        "between both frames?" + "<br>" + "<br>" + "<b>" + "If no: " + "</b>" +
                        "Try a larger window size." + "<b>" + "<br>" + "If yes, but there are more " +
                        "than 15 particles: " + "</b>" + "Try a smaller window size." + "<br>" +
                        "<b>" + "Yes, and I can see how much they moved between the two frames: " +
                        "</b>" + "Choose this window size in the next step!"))
                .setView(popupLayout)
                .setPositiveButton("OK", yesListener);
    }

    private void buildView() {
        PhotoView densityAnimView = popupLayout.findViewById(R.id.densityImage);
        densityAnimView.clearAnimation();
        densityAnimView.setImageDrawable(createAnimation());
    }

    private AnimationDrawable createAnimation() {
        Bitmap bmp1 = BitmapFactory.decodeFile(frame1Path);
        Bitmap bmp2 = BitmapFactory.decodeFile(frame2Path);

        // Shrinking bitmap
        Bitmap cropped1 = getBitmapResized(getCroppedBitmap(bmp1), cropSize, cropSize);
        Bitmap cropped2 = getBitmapResized(getCroppedBitmap(bmp2), cropSize, cropSize);
        // Expanding bitmap
        Bitmap pixelated1 = getBitmapResized(cropped1, 700, 700);
        Bitmap pixelated2 = getBitmapResized(cropped2, 700, 700);

        AnimationDrawable animation = new AnimationDrawable();

        Drawable d1 = new BitmapDrawable(context.getResources(), pixelated1);
        Drawable d2 = new BitmapDrawable(context.getResources(), pixelated2);
        Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
        animation.addFrame(d1,1000);
        animation.addFrame(d2,1000);
        animation.addFrame(transparentDrawable, 1);
        animation.addFrame(d1,1);
        animation.setOneShot(false);
        animation.start();
        return animation;
    }

    private Bitmap getCroppedBitmap(Bitmap toBeCropped) {
        Bitmap cropped1 = Bitmap.createBitmap(
                toBeCropped,
                Math.round(toBeCropped.getWidth()/2f), Math.round(toBeCropped.getHeight()/2f),
                cropSize, cropSize);
        return PivFunctions.resizeBitmap(cropped1, 600);
    }

    public Bitmap getBitmapResized(Bitmap bitmap, int newW, int newH) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float rescaledW = (float)newW / width;
        float rescaledH = (float)newH / height;

        Matrix bitmapMatrix = new Matrix();
        bitmapMatrix.postScale(rescaledW, rescaledH);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, bitmapMatrix, false);
    }
}