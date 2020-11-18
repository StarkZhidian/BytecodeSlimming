package com.hiro.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import androidx.annotation.Nullable;

class DoNothingViewSubClass extends DoNothingView {

    private static final String TAG = "DoNothingViewSubClass";

    public DoNothingViewSubClass(Context context) {
        super(context);
    }

    public DoNothingViewSubClass(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DoNothingViewSubClass(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        new InnerClass();
    }

    private class InnerClass {
        InnerClass() {
            Log.d(TAG, "subclassVisitField: " + subclassVisitField);
        }
    }
}
