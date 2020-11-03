package com.hiro.demo.superclass;

import android.util.Log;

public class InnerSuperClassMethodInvoke {
    private static final String TAG = "InnerSuperClassMethodIn";


    InnerSuperSuperClass superSuperClass = new InnerSuperSuperClass();

    public InnerSuperClassMethodInvoke() {
        Log.d(TAG, "" + superSuperClass.getValue());
    }

}
