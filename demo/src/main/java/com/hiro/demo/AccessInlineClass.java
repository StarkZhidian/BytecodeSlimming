package com.hiro.demo;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.hiro.demo.getter_setter.DataModel;

public class AccessInlineClass {

    private static final String TAG = "AccessInlineClass";

    @NonNull
    Context context;

    public AccessInlineClass(@NonNull Context context) {
        this.context = context;

        staticTest();
    }

    static void staticTest() {
        int y = new InnerClass().x;
        InnerClass innerClass = new InnerClass();
        innerClass.x = 2;
        innerClass.staticX = 1;
        innerClass.longX = 3L;
        innerClass.setStaticX(4);
        innerClass.setDataModel(new DataModel()).setName("dataModelName");
        int z = new InnerClass().getField();
        int xx = new InnerClass().getX();
        int yy = new InnerClass().getY();
        int staticField = InnerClass.getStaticFiled();

        Log.d(TAG, "y = " + y + ", z = " + z + ", xx = " + xx + ", yy = " + yy + ", staticFiled = " + staticField + ", getSuperClassX = " + new InnerClass().getSuperX());
    }


    public void onCreate() {

    }

    private static class InnerClass extends InnerSuperClass {
        protected int x;
        private long longX;
        private String s;
        private DataModel dataModel = new DataModel();
        int y = 1023;
        private int z;

        private static int staticX;

        int getX() {
            return x;
        }

        private int getY() {
            int y = getField();
            return y;
        }

        private DataModel setDataModel(DataModel dataModel) {
            Log.d(TAG, "setDataModel() called with: dataModel = [" + dataModel + "]");
            return new DataModel();
        }

        @NonNull
        private int getField() {
            return 1024;
        }

        private static int getStaticFiled() {
            return 1025;
        }

        private static void setStaticX(int staticX) {
            InnerClass.staticX = staticX;
        }

    }
}
