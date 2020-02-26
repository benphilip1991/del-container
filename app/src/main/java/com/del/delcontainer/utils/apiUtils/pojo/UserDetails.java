package com.del.delcontainer.utils.apiUtils.pojo;

import com.google.gson.annotations.SerializedName;

public class UserDetails {

    @SerializedName("_id")
    private String _id;

    @SerializedName("emailId")
    private String emailId;

    @SerializedName("age")
    private int age;

    @SerializedName("creationDate")
    private String creationDate;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("sex")
    private String sex;

    @SerializedName("userRole")
    private String userRole;

    final String password;

    public UserDetails(String emailId, String firstName,
                       String lastName, int age, String sex,
                       String userRole, String password) {
        this.emailId = emailId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.sex = sex;
        this.userRole = userRole;
        this.password = password;
    }

    public String get_id() {
        return _id;
    }

    public String getEmailId() {
        return emailId;
    }

    public int getAge() {
        return age;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSex() {
        return sex;
    }

    public String getUserRole() {
        return userRole;
    }
}
