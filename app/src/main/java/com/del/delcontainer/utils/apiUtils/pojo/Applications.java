package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Applications {

    @SerializedName("applications")
    private ArrayList<ApplicationDetails> applications;

    public ArrayList<ApplicationDetails> getApplications() {
        return applications;
    }
}
