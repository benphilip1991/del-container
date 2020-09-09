package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LinkedApplicationDetails {

    @SerializedName("applicationId")
    private String applicationId;

    @SerializedName("addedBy")
    private String addedBy;

    @SerializedName("addedOn")
    private String addedOn;

    @SerializedName("applicationName")
    private String applicationName;

    @SerializedName("applicationUrl")
    private String applicationUrl;

    @SerializedName("applicationPermissions")
    private AccessPermissionsList applicationPermissions;

    private class AccessPermissionsList {
        @SerializedName("accessPermissions")
        private List<String> accessPermissions;

        public List<String> getAccessPermissions() { return accessPermissions; }

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

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public List<String> getApplicationPermissions() {
        return applicationPermissions.accessPermissions;
    }
}
