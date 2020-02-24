package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Token details object
 */
public class TokenDetails {

    @SerializedName("userId")
    private String userId;

    @SerializedName("userRole")
    private String userRole;

    @SerializedName("statusCode")
    private String statusCode;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("attributes")
    private Attributes attributes;

    private class Attributes {
        @SerializedName("error")
        private String error;

        public String getError() {
            return error;
        }
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }
}
