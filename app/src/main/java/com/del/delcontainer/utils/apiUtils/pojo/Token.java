package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

public class Token {

    @SerializedName("bearer")
    private String token;

    public String getToken() {
        return token;
    }
}
