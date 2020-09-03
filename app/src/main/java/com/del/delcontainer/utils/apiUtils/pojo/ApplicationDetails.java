package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplicationDetails {

    @SerializedName("_id")
    private String _id;

    @SerializedName("DeveloperId")
    private String developerId;

    @SerializedName("applicationName")
    private String applicationName;

    @SerializedName("applicationDescription")
    private String applicationDescription;

    @SerializedName("applicationUrl")
    private String applicationUrl;

    @SerializedName("applicationRegistrationDate")
    private String applicationRegistrationDate;

    @SerializedName("dataDescription")
    private DataCollectedList dataDescription;

    @SerializedName("applicationPermissions")
    private AccessPermissionsList applicationPermissions;


    private class DataCollectedList {

        @SerializedName("dataCollected")
        private ArrayList<DataCollected> dataCollected;

        public ArrayList<DataCollected> getDataCollected() {
            return dataCollected;
        }
    }

    private class AccessPermissionsList {
        @SerializedName("accessPermissions")
        private List<String> accessPermissions;

        public List<String> getAccessPermissions() { return accessPermissions; }

    }

    private class DataCollected {

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

    public String get_id() {
        return _id;
    }

    public String getDeveloperId() {
        return developerId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public DataCollectedList getDataDescription() {
        return dataDescription;
    }

    public List<String> getApplicationPermissions() {
        return applicationPermissions.accessPermissions;
    }

    public String getApplicationRegistrationDate() {
        return applicationRegistrationDate;
    }
}
