package com.hiro.demo;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

public class AccessInlineClass {

    private static final String TAG = "AccessInlineClass";

    @NonNull
    Context context;

    public AccessInlineClass(@NonNull Context context) {
        this.context = context;

        int y = new InnerClass().x;
        int z = new InnerClass().getField();
        int xx = new InnerClass().getX();
        int yy = new InnerClass().getY();
        int staticField = InnerClass.getStaticFiled();

        Log.d(TAG, "y = " + y + ", z = " + z + ", xx = " + xx + ", yy = " + yy + ", staticFiled = " + staticField + ", getSuperClassX = " + new InnerClass().getSuperX());


    }


    public void onCreate() {

    }

    private static class InnerClass extends InnerSuperClass {
        private int x;
        int y = 1023;
        private int z;

        int getX() {
            return x;
        }

        private int getY() {
            return y;
        }

        @NonNull
        private int getField() {
            return 1024;
        }

        private static int getStaticFiled() {
            return 1025;
        }

    }
}
