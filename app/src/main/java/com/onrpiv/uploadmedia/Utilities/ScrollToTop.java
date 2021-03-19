package com.onrpiv.uploadmedia.Utilities;

import android.os.Handler;
import android.view.View;

import androidx.core.widget.NestedScrollView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScrollToTop {

    private NestedScrollView scrollView;
    private FloatingActionButton fab;

    public ScrollToTop(NestedScrollView myScrollView, FloatingActionButton myFab) {
        scrollView = myScrollView;
        fab = myFab;
    }

    public void scrollFunction() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.scrollTo(0, 0);
            }
        });

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int dy = scrollY - oldScrollY;

                // scrolling down
                if (dy > 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fab.setVisibility(View.GONE);
                        }
                    }, 2000); // delay of 2 seconds before hiding the fab
                }

                // scrolling up
                else if (dy < 0) {
                    fab.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
