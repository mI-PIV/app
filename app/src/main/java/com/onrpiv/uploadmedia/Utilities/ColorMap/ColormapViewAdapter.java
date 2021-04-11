package com.onrpiv.uploadmedia.Utilities.ColorMap;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.onrpiv.uploadmedia.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;



public class ColormapViewAdapter extends RecyclerView.Adapter<ColormapViewAdapter.ViewHolder> {

    private ColorMapPicker.OnFastChooseColorListener onFastChooseColorListener;
    private ArrayList<ColorMapPal> mDataset;
    private int colorPosition = -1;
    private Drawable colorSelected;
    private int marginLeft, marginRight, marginTop, marginBottom;
    private int tickColor = Color.WHITE;
    private int marginButtonLeft = 0, marginButtonRight = 0, marginButtonTop = 3, marginButtonBottom = 3;
    private int buttonWidth = -1, buttonHeight = -1;
    private int buttonDrawable;
    private WeakReference<CustomDialog> mDialog;


    public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public AppCompatButton colorItem;

        public ViewHolder(View v) {
            super(v);
            //buttons settings
            colorItem = v.findViewById(R.id.color);
            colorItem.setTextColor(tickColor);
            colorItem.setBackgroundResource(buttonDrawable);
            colorItem.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) colorItem.getLayoutParams();
            layoutParams.setMargins(marginButtonLeft, marginButtonTop, marginButtonRight, marginButtonBottom);
            if (buttonWidth != -1)
                layoutParams.width = buttonWidth;
            if (buttonHeight != -1)
                layoutParams.height = buttonHeight;

            //relative layout settings
            LinearLayout linearLayout = v.findViewById(R.id.linearLayout);
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) linearLayout.getLayoutParams();
            lp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        }

        @Override
        public void onClick(View v) {
            if (colorPosition != -1 && colorPosition != getLayoutPosition()) {
                mDataset.get(colorPosition).setCheck(false);
                notifyItemChanged(colorPosition);
            }
            colorPosition = getLayoutPosition();
            colorSelected = (Drawable) v.getTag();
            mDataset.get(getLayoutPosition()).setCheck(true);
            notifyItemChanged(colorPosition);

            if (onFastChooseColorListener != null && mDialog != null) {
                onFastChooseColorListener.setOnFastChooseColorListener(colorPosition, colorSelected);
                dismissDialog();
            }
        }
    }

    private void dismissDialog() {
        if(mDialog == null)
            return;
        Dialog dialog = mDialog.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public Drawable getColorSelected() {
        return colorSelected;
    }

    public int getColorPosition() {
        return colorPosition;
    }

    public ColormapViewAdapter(ArrayList<ColorMapPal> myDataset, ColorMapPicker.OnFastChooseColorListener onFastChooseColorListener, WeakReference<CustomDialog> dialog) {
        mDataset = myDataset;
        mDialog = dialog;
        this.onFastChooseColorListener = onFastChooseColorListener;
    }

    public ColormapViewAdapter(ArrayList<ColorMapPal> myDataset) {
        mDataset = myDataset;
    }


    @Override
    public ColormapViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.palette_item, parent, false);
        return new ColormapViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ColormapViewAdapter.ViewHolder holder, int position) {
        Drawable color = mDataset.get(position).getDrawable();

//        int textColor = ColorUtils.isWhiteText(color) ? Color.WHITE : Color.BLACK;
        int textColor = Color.BLACK;

        if (mDataset.get(position).isCheck()) {
            holder.colorItem.setText("✔");
        }

        holder.colorItem.setTextColor(tickColor == Color.WHITE ? textColor : tickColor);
        if (buttonDrawable != 0) {
            holder.colorItem.setBackgroundDrawable(color);
        } else {
            holder.colorItem.setBackgroundDrawable(color);
        }
        holder.colorItem.setTag(color);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public void setMargin(int left, int top, int right, int bottom) {
        this.marginBottom = bottom;
        this.marginLeft = left;
        this.marginRight = right;
        this.marginTop = top;
    }

    public void setDefaultColor(Drawable color) {
        for (int i = 0; i < mDataset.size(); i++) {
            ColorMapPal colorPal = mDataset.get(i);
            if (colorPal.getDrawable() == color) {
                colorPal.setCheck(true);
                colorPosition = i;
                notifyItemChanged(i);
            }
        }
    }

    public void setTickColor(int color) {
        this.tickColor = color;
    }

    public void setColorButtonMargin(int left, int top, int right, int bottom) {
        this.marginButtonLeft = left;
        this.marginButtonRight = right;
        this.marginButtonTop = top;
        this.marginButtonBottom = bottom;
    }

    public void setColorButtonSize(int width, int height) {
        this.buttonWidth = width;
        this.buttonHeight = height;
    }

    public void setColorButtonDrawable(int drawable) {
        this.buttonDrawable = drawable;
    }

}