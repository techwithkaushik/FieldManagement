package com.ask2784.fieldmanagement.databases.models;

public class FieldDetails {
    private String year;
//            ,crop,water,implement;

    public FieldDetails() {
    }

    public FieldDetails(String year) {
        this.year = year;

    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
