package com.del.delcontainer.ui.services;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.del.delcontainer.utils.apiUtils.APIUtils;
import com.del.delcontainer.utils.apiUtils.interfaces.ApplicationApi;
import com.del.delcontainer.utils.apiUtils.pojo.ApplicationDetails;
import com.del.delcontainer.utils.apiUtils.pojo.Applications;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ServicesViewModel extends ViewModel {

    private static final String TAG = "ServicesViewModel";
    private MutableLiveData<ArrayList<ApplicationDetails>> servicesList = new MutableLiveData<>();

    Retrofit retrofit = APIUtils.getApiClient();
    ApplicationApi applicationApi = retrofit.create(ApplicationApi.class);

    public MutableLiveData<ArrayList<ApplicationDetails>> getServicesList() {
        return servicesList;
    }

    /**
     * Fetch list of available apps. The response is serialized into the POJO
     * Applications which itself describes a list of Applications
     *
     * @param token
     */
    public void getAllAvailableServices(String token) {

        Call<Applications> call = applicationApi.getAllApplications(token);
        call.enqueue(new Callback<Applications>() {
            @Override
            public void onResponse(Call<Applications> call, Response<Applications> response) {
                if(response.code() == 200) {
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
}