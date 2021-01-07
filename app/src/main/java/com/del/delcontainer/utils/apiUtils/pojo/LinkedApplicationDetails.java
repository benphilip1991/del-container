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

    @SerializedName("dataDescription")
    private DataCollectedList dataDescription;

    @SerializedName("applicationPermissions")
    private AccessPermissionsList applicationPermissions;

    private class AccessPermissionsList {
        @SerializedName("accessPermissions")
        private List<String> accessPermissions;

        public List<String> getAccessPermissions() { return accessPermissions; }

    }

    private class DataCollectedList {

        @SerializedName("dataCollected")
        private List<DataCollectedDefinitions> dataCollectedDefs;

        public List<DataCollectedDefinitions> getDataCollectedDefs() {
            return dataCollectedDefs;
        }
    }

    /**
     * Need to make this class public to enable access to data types and permissions
     */
    public class DataCollectedDefinitions {

        @SerializedName("dataType")
        private String dataType;

        @SerializedName("description")
        private String description;

        public String getDataType() {
            return dataType;
        }

        public String getDescription() {
            return description;
        }
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

    public List<DataCollectedDefinitions> getDataDescription() {
        return dataDescription.getDataCollectedDefs();
    }
}
