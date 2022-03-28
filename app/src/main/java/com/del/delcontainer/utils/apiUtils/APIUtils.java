package com.del.delcontainer.utils.apiUtils;

import com.del.delcontainer.utils.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIUtils {

    private static Retrofit retrofit = null;

    private static OkHttpClient.Builder builder = new OkHttpClient().newBuilder().hostnameVerifier(
            (hostname, sslSession) -> true
    );
    private static OkHttpClient okHttpClient = builder.build();

    private static final String delUrl = Constants.HTTP_PREFIX +
            Constants.DEL_SERVICE_IP + ":" + Constants.DEL_PORT +
            Constants.API_BASE_PATH;

    public static Retrofit getApiClient() {
        if (null == retrofit) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(delUrl)
                    //.client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
