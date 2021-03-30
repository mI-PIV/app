package com.onrpiv.uploadmedia.Utilities.ColorMap;


import android.graphics.drawable.Drawable;

public class ColorMapPal {
    private Drawable drawable;
    private boolean check;

    public ColorMapPal(Drawable color, boolean check) {
        this.drawable = color;
        this.check = check;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ColorMapPal && ((ColorMapPal) o).drawable == drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
