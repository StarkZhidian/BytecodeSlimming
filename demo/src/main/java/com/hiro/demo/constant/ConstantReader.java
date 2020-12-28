package com.hiro.demo.constant;

import android.util.Log;

/**
 * 常量读取类
 */
public class ConstantReader {

    public static void main(String[] args) {
        boolean b = Constants.BOOLEAN_CONSTANT;
        System.out.println(b);

        byte by = Constants.BYTE_CONSTANT;
        System.out.println(by);

        char c = Constants.CHAR_CONSTANT;
        System.out.println(c);

        short s = Constants.SHORT_CONSTANT;
        System.out.println(s);

        int i = Constants.INT_CONSTANT;
        System.out.println(i);

        long l = Constants.LONG_CONSTANT;
        System.out.println(l);

        long currentTime = Constants.CURRENT_TIME;
        System.out.println(currentTime);

        float f = Constants.FLOAT_CONSTANT;
        System.out.println(f);

        double d = Constants.DOUBLE_CONSTANT;
        System.out.println(d);
        
        String str = Constants.STRING_CONSTANT;
        System.out.println(str);

        int[] intArray = Constants.INT_ARRAY_CONSTANT;
        System.out.println(intArray);
    }

}
