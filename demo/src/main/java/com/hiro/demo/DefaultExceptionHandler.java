package com.hiro.demo;

import android.util.Log;
import androidx.annotation.NonNull;

/**
 * 默认的 crash 处理器（当某个 crash 没有被任何的线程处理时）会派发给这个类处理
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "DefaultExceptionHandler";

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e(TAG, "", e);
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            Log.e(TAG, "uncaughtException() called with: stackTraceElement at i = " + element
                    + ", declearingClass = " + element.getClassName());
        }
    }
}
