package com.onrpiv.uploadmedia.Learn;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.onrpiv.uploadmedia.R;

public class PIVBasics3 extends PIVBasicsLayout {

    private int headerTextSize = 25;
    private int paraTextSize = 16;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pivbasics3);

    }
}
