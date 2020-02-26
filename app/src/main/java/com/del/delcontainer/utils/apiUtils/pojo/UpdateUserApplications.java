package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

public class UpdateUserApplications {

    @SerializedName("applicationId")
    final String applicationId;

    @SerializedName("operation")
    final String operation;

    public UpdateUserApplications(String applicationId, String operation) {
        this.applicationId = applicationId;
        this.operation = operation;
    }
}
