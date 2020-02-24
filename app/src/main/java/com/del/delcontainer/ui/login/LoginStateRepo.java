package com.del.delcontainer.ui.login;

/**
 * Holds the currently logged in user's token
 * details.
 */
public class LoginStateRepo {

    private String token;
    private String userId = null;
    private String emailId = null;
    private String firstName = null;
    private String lastName = null;
    private String sex = null;
    private String userRole = null;
    private int age;

    private static LoginStateRepo loginStateRepo = null;

    public static LoginStateRepo getInstance() {
        if(null == loginStateRepo) {
            loginStateRepo = new LoginStateRepo();
        }

        return loginStateRepo;
    }

    private LoginStateRepo() {;}

    public String getEmailId() {
        return emailId;
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

    public int getAge() {
        return age;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
