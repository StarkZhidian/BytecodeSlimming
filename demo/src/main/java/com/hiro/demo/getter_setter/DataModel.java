package com.hiro.demo.getter_setter;

import java.util.List;

public class DataModel {

    private static final String TAG = "DataModel";

    private int id;

    private String name;

    private static List<DataModel> subData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataModel> getSubData() {
        return subData;
    }

    public void setSubData(List<DataModel> subData) {
        this.subData = subData;
    }

    public String getTAG() {
        return TAG;
    }

    public String getName2() {
        return getNamePrivate();
    }

    private String getNamePrivate() {
        return getName();
    }
}
