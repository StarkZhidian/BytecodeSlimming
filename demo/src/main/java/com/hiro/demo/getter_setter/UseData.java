package com.hiro.demo.getter_setter;

import android.util.Log;
import java.util.List;

public class UseData {

    private static final String TAG = "UseData";


    private DataModel dataModel = new DataModel();


    public void onCreate() {
        int id = dataModel.getId();

        String name = dataModel.getName();

        String name2 = dataModel.getName2();

        dataModel.setSubData(null);

        String tag = dataModel.getTAG();

        dataModel.setId(2);

        Log.d(TAG, "id = " + id + ", name = " + name + ", name2 = " + name2 + ", subData = " + ", TAG = " + tag);


    }

}
