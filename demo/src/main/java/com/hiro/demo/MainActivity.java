package com.hiro.demo;

import android.app.Activity;
import android.os.Bundle;
import com.hiro.demo.getter_setter.UseData;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        new AccessInlineClass(this).onCreate();
        new UseData().onCreate();
    }
}
