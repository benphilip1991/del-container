package com.del.delcontainer.ui.settings;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.del.delcontainer.utils.apiUtils.APIUtils;
import com.del.delcontainer.utils.apiUtils.interfaces.UserApi;
import com.del.delcontainer.utils.apiUtils.pojo.UserDetails;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SettingsViewModel extends ViewModel {

    private static final String TAG = "SettingsViewModel";
    private MutableLiveData<String> firstName = new MutableLiveData<>();
    Retrofit retrofit = APIUtils.getApiClient();

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
                if(response.code() == 200) {
                    Log.d(TAG, "onResponse: Got first name");
                    firstName.setValue(response.body().getFirstName());
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
}
