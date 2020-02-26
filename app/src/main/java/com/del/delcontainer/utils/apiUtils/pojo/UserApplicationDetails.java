package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UserApplicationDetails {

    @SerializedName("userId")
    private String userId;

    @SerializedName("applications")
    private ArrayList<LinkedApplicationDetails> applications;

    public String getUserId() {
        return userId;
    }

    public ArrayList<LinkedApplicationDetails> getApplications() {
        return applications;
    }
}
