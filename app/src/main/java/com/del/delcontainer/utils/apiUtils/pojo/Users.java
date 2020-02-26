package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Users {

    @SerializedName("users")
    private ArrayList<UserDetails> users;

    public ArrayList<UserDetails> getUsers() {
        return users;
    }
}
