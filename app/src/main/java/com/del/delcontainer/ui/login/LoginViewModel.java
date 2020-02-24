package com.del.delcontainer.ui.login;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.del.delcontainer.utils.apiUtils.APIUtils;
import com.del.delcontainer.utils.apiUtils.interfaces.AuthTokenApi;
import com.del.delcontainer.utils.apiUtils.pojo.Token;
import com.del.delcontainer.utils.apiUtils.pojo.TokenDetails;
import com.del.delcontainer.utils.apiUtils.pojo.UserCredentials;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.Call;

/**
 * ViewModel instance to manage login form data.
 * This is not really required, but helps retain retain
 * data on configuration change.
 */
public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";

    private MutableLiveData<LoginStateRepo> loginStateRepo = new MutableLiveData<>();

    public MutableLiveData<LoginStateRepo> getLoginStateRepo() {
        return loginStateRepo;
    }

    Retrofit retrofit = APIUtils.getApiClient();
    AuthTokenApi authTokenApi = retrofit.create(AuthTokenApi.class);

    public void login(String emailId, String password) {

        UserCredentials userCredentials = new UserCredentials(emailId, password);
        Call<Token> call = authTokenApi.getAuthToken(userCredentials);

        // Enqueue for async
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.code() == 200) {
                    Log.d(TAG, "onResponse: Token : Bearer " + response.body().getToken());
                    LoginStateRepo.getInstance().setToken("Bearer " + response.body().getToken());
                    loginStateRepo.setValue(LoginStateRepo.getInstance());
                } else {
                    Log.e(TAG, "onResponse: Error " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Log.e(TAG, "onResponse: Error " + t.getMessage());
            }
        });
    }

    public boolean getUserTokenDetails(String token) {

        Call<TokenDetails> call = authTokenApi.getTokenDetails(token);
        call.enqueue(new Callback<TokenDetails>() {
            @Override
            public void onResponse(Call<TokenDetails> call, Response<TokenDetails> response) {
                if (response.code() == 200) {
                    Log.d(TAG, "onResponse: Got user details");
                    LoginStateRepo.getInstance().setUserId(response.body().getUserId());
                    LoginStateRepo.getInstance().setUserRole(response.body().getUserRole());
                    loginStateRepo.setValue(LoginStateRepo.getInstance());
                } else {
                    Log.e(TAG, "onResponse: Error " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TokenDetails> call, Throwable t) {
                Log.e(TAG, "onResponse: Error " + t.getMessage());
            }
        });

        return true;
    }

}
