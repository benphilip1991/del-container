package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UserApplicationDetails {

    @SerializedName("userId")
    private String userId;

    @SerializedName("applications")
    private ArrayList<ApplicationLinkDetails> applications;

    public String getUserId() {
        return userId;
    }

    public ArrayList<ApplicationLinkDetails> getApplications() {
        return applications;
    }

    private class ApplicationLinkDetails {

        @SerializedName("_id")
        private String _id;

        @SerializedName("applicationId")
        private String applicationId;

        @SerializedName("addedBy")
        private String addedBy;

        @SerializedName("addedOn")
        private String addedOn;

        public String get_id() {
            return _id;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getAddedBy() {
            return addedBy;
        }

        public String getAddedOn() {
            return addedOn;
        }
    }
}
