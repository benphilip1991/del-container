package com.del.delcontainer.utils.apiUtils.interfaces;

import com.del.delcontainer.utils.apiUtils.pojo.Token;
import com.del.delcontainer.utils.apiUtils.pojo.TokenDetails;
import com.del.delcontainer.utils.apiUtils.pojo.UserCredentials;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthTokenApi {

    @POST("auth")
    @Headers({"Content-Type: application/json"})
    Call<Token> getAuthToken(@Body UserCredentials userCredentials);

    @GET("auth")
    @Headers({"Content-Type: application/json"})
    Call<TokenDetails> getTokenDetails(@Header("Authorization") String token);
}
