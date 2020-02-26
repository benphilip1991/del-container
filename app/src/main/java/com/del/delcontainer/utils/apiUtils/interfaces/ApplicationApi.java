package com.del.delcontainer.utils.apiUtils.interfaces;

import com.del.delcontainer.utils.apiUtils.pojo.ApplicationDetails;
import com.del.delcontainer.utils.apiUtils.pojo.Applications;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ApplicationApi {

    @GET("application")
    @Headers({"Content-Type: application/json"})
    Call<Applications> getAllApplications(@Header("Authorization") String token);

    @GET("application/{applicationId}")
    @Headers({"Content-Type: application/json"})
    Call<ApplicationDetails> getSingleApplicationDetails(@Header("Authorization") String token,
                                                         @Path("applicationId") String applicationId);
}
