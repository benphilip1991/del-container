package com.del.delcontainer.utils.apiUtils.pojo;

public class UpdateUserApplications {

    final String applicationId;
    final Operations operation;

    public UpdateUserApplications(String applicationId, Operations operation) {
        this.applicationId = applicationId;
        this.operation = operation;
    }

    public enum Operations {
        ADD("add"),
        DELETE("delete");

        public final String value;

        private Operations(String value) {
            this.value = value;
        }
    }
}
