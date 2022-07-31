package com.ask2784.fieldmanagement.databases.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Fields implements Serializable {

    private String uId, name, area;

    public Fields() {
    }

    public Fields(String uId, String name,
                  String area) {
        this.uId = uId;
        this.name = name;
        this.area = area;
    }

    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @NonNull
    @Override
    public String toString() {
        return "Fields [area=" + area
                + ", name=" + name + ", uId=" + uId + "]";
    }

}