package com.hiro.demo;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyClass {

    private static final String TAG = "MyClass";

    @NonNull
    Context context;

    public MyClass(@NonNull Context context) {
        this.context = context;

        int y = new InnerClass().x;
        int z = new InnerClass().getField();
        Log.d(TAG, "y = " + y + ", z = " + z);
    }


    public void onCreate() {

    }

    private class InnerClass {
        private int x;

        @NonNull
        private int getField() {
            return 1024;
        }
    }
}
