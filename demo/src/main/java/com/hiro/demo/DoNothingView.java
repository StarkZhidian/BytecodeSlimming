package com.hiro.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * 内部类调用 android 源码中的 protect 方法时产生的 access$ 方法禁止内联测试
 */
public class DoNothingView extends View  {

    private DoMeasureInnerClass doMeasureInnerClass = new DoMeasureInnerClass();

    public DoNothingView(Context context) {
        super(context);
    }

    public DoNothingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DoNothingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        doMeasureInnerClass.setMeasureDimension();
    }

    private class DoMeasureInnerClass {
        void setMeasureDimension() {
            // 这里调用了外部类的 super 中的 protect 方法
            setMeasuredDimension(100, 100);
        }
    }
}
