package com.del.delcontainer.utils.apiUtils.interfaces;

import com.del.delcontainer.utils.apiUtils.pojo.UpdateUserApplications;
import com.del.delcontainer.utils.apiUtils.pojo.UserApplicationDetails;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApplicationsApi {

    @GET("user/{userId}/userApplication")
    @Headers({"Content-Type: application/json"})
    Call<UserApplicationDetails> getUserApplicationDetails(@Header("Authorization") String token,
                                                           @Path("userId") String userId);

    @PUT("user/{userId}/userApplication")
    @Headers({"Content-Type: application/json"})
    Call<UserApplicationDetails> updateUserApplicationDetails(
            @Header("Authorization") String token,
            @Path("userId") String userId,
            @Body UpdateUserApplications updateUserApplications);
}
