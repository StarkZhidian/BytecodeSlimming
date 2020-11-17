package com.hiro.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.hiro.demo.getter_setter.UseData;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private TextView helloTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        new AccessInlineClass(this).onCreate();
        new UseData().onCreate();

        helloTv = findViewById(R.id.tv_test_npe);
        helloTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick() called with: v = 1");
                String msg = null;
                msg.length();
            }
        });
        new OperateSuperFieldInnerClass().visitField();
    }

    /**
     * 内部类访问外部类 android 源码父类中的 protected 字段禁止内联测试
     */
    private class OperateSuperFieldInnerClass {

        void visitField() {
            int[] focusedStateSet = FOCUSED_STATE_SET;
            Log.d(TAG, "" + focusedStateSet);
        }
    }
}
