package com.del.delcontainer.ui.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.apiUtils.APIUtils;
import com.del.delcontainer.utils.apiUtils.interfaces.ApplicationApi;
import com.del.delcontainer.utils.apiUtils.interfaces.UserApi;
import com.del.delcontainer.utils.apiUtils.interfaces.UserApplicationsApi;
import com.del.delcontainer.utils.apiUtils.pojo.ApplicationDetails;
import com.del.delcontainer.utils.apiUtils.pojo.Applications;
import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;
import com.del.delcontainer.utils.apiUtils.pojo.UpdateUserApplications;
import com.del.delcontainer.utils.apiUtils.pojo.UserApplicationDetails;
import com.del.delcontainer.utils.apiUtils.pojo.UserDetails;

import java.util.ArrayList;
import java.util.Hashtable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ServicesViewModel extends ViewModel {

    private static final String TAG = "ServicesViewModel";
    private MutableLiveData<ArrayList<ApplicationDetails>> servicesList = new MutableLiveData<>();
    private MutableLiveData<ArrayList<LinkedApplicationDetails>> userServicesList =
            new MutableLiveData<>();
    private MutableLiveData<String> firstName = new MutableLiveData<>();
    private MutableLiveData<String> status = new MutableLiveData<>();
    private String statusMessage = "";
    Retrofit retrofit = APIUtils.getApiClient();

    public MutableLiveData<ArrayList<ApplicationDetails>> getServicesList() {
        return servicesList;
    }

    public MutableLiveData<ArrayList<LinkedApplicationDetails>> getUserServicesList() {
        return userServicesList;
    }

    /**
     * Fetch list of available apps. The response is serialized into the POJO
     * Applications which itself describes a list of Applications
     *
     * @param token
     */
    public void getAllAvailableServices(String token) {

        ApplicationApi applicationApi = retrofit.create(ApplicationApi.class);
        Call<Applications> call = applicationApi.getAllApplications(token);
        call.enqueue(new Callback<Applications>() {
            @Override
            public void onResponse(Call<Applications> call, Response<Applications> response) {
                if (response.code() == Constants.HTTP_SUCCESS) {
                    Log.d(TAG, "onResponse: Got applications.");
                    servicesList.setValue(response.body().getApplications());
                } else {
                    Log.e(TAG, "onResponse: Error " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Applications> call, Throwable t) {
                Log.e(TAG, "onResponse: Error " + t.getMessage());
            }
        });
    }

    /**
     * Fetch list of applications linked to a user account. The response is a
     *
     * @param token
     * @param userId
     */
    public void getAllUserServices(String token, String userId) {

        UserApplicationsApi userApplicationsApi = retrofit.create(UserApplicationsApi.class);
        Call<UserApplicationDetails> call = userApplicationsApi
                .getUserApplicationDetails(token, userId);

        call.enqueue(new Callback<UserApplicationDetails>() {
            @Override
            public void onResponse(Call<UserApplicationDetails> call,
                                   Response<UserApplicationDetails> response) {
                if (response.code() == Constants.HTTP_SUCCESS) {
                    Log.d(TAG, "onResponse: Got linked applications");
                    userServicesList.setValue(response.body().getApplications());
                } else {
                    Log.e(TAG, "onResponse: Error linked applications " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserApplicationDetails> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    /**
     * Update user service list - this can be adding or removing services.
     *
     * @param token
     * @param userId
     * @param appId
     * @param operation
     */
    public void updateUserApplicationsList(String token, String userId,
                                           String appId, String operation) {

        UserApplicationsApi userApplicationsApi = retrofit.create(UserApplicationsApi.class);
        Call<UserApplicationDetails> call = userApplicationsApi
                .updateUserApplicationDetails(token, userId, new UpdateUserApplications(appId,
                        operation));

        call.enqueue(new Callback<UserApplicationDetails>() {
            @Override
            public void onResponse(Call<UserApplicationDetails> call, Response<UserApplicationDetails> response) {
                if(response.code() == Constants.HTTP_SUCCESS) {
                    userServicesList.setValue(response.body().getApplications());
                } else {
                    if(response.code() == Constants.HTTP_BAD_REQUEST) {
                        statusMessage = "Application already installed";
                        status.setValue(Constants.DIALOG_ERROR);
                        Log.e(TAG, "App already linked");
                    }
                }
            }

            @Override
            public void onFailure(Call<UserApplicationDetails> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    /**
     * Fetch first name of the user
     *
     * @param token
     * @param userId
     */
    public void getUserFirstName(String token, String userId){
        UserApi userApi = retrofit.create(UserApi.class);
        Call<UserDetails> call = userApi.getSingleUserDetails(token, userId);

        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if(response.code() == Constants.HTTP_SUCCESS) {
                    Log.d(TAG, "onResponse: Got first name");
                    firstName.setValue("Hello "+response.body().getFirstName());
                }
                else {
                    Log.e(TAG, "onResponse: Error getting first name" + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public LiveData<String> getFirstName() { return firstName; }
    public LiveData<String> getStatusObserver(){ return status; }
    public String getStatusMessage() { return statusMessage; }
}