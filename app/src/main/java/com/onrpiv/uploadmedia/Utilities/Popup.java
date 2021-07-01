package com.onrpiv.uploadmedia.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public abstract class Popup implements View.OnClickListener {
    protected final View popupView;
    private final AlertDialog alertDialog;


    public Popup(final Context context, final int layoutID, String title, boolean setCancelable) {
        // create popup view
        LayoutInflater inflater = LayoutInflater.from(context);
        popupView = inflater.inflate(layoutID, null);

        // create alert dialog popup
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(setCancelable);
        builder.setView(popupView);
        alertDialog = builder.create();

        View[] views = initOnClickViews();
        setOnClickListeners(views);
    }

    public void show() {
        alertDialog.show();
    }

    protected void closePopup() {
        alertDialog.cancel();
    }

    protected abstract View[] initOnClickViews();

    private void setOnClickListeners(View[] viewsNeedingListeners) {
        for (View v : viewsNeedingListeners) {
            v.setOnClickListener(this);
        }
    }
}
