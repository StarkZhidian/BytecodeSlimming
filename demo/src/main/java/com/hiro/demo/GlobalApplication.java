package com.hiro.demo;

import android.app.Application;
import android.content.Context;

public class GlobalApplication extends Application {

    private DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler);
    }
}
