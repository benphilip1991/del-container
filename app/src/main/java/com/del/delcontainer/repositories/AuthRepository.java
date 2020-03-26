package com.del.delcontainer.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.del.delcontainer.database.DelDatabase;
import com.del.delcontainer.database.dao.AuthDao;
import com.del.delcontainer.database.entities.Auth;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static AuthRepository instance;
    private static AuthDao authDao;
    private static Auth authInfo;
    private static String accessToken = "";
    private static String refreshToken = "";
    private static String userId = "";
    private static MutableLiveData<String> token = new MutableLiveData<>();

    private AuthRepository() {
        ;
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (null == instance) {
            instance = new AuthRepository();
            DelDatabase database = DelDatabase.getInstance(context);
            authDao = database.authDao();
        }
        return instance;
    }

    public void updateAccessToken(String accessToken) {
        new UpdateAccessTokenAsyncTask(authDao).execute(accessToken);
    }

    public void addAuthInfo(Auth auth) {
        new AddAuthInfoAsyncTask(authDao).execute(auth);
    }

    public Auth getAuthInfo() {
        new GetAuthInfoAsyncTask(authDao).execute();
        return authInfo;
    }

    public void clearAuthInfo() {
        new ClearAuthInfoAsyncTask(authDao).execute();
    }

    public String getAccessToken() {
        String token = null;
        try {
            token = new GetAccessTokenAsyncTask(authDao).execute().get();
        } catch(Exception e) {
            Log.d(TAG, "getAccessToken: " + e.getMessage());
        }
        return token;
    }

    public String getRefreshToken() {
        new GetRefreshTokenAsyncTask(authDao).execute();
        return refreshToken;
    }

    public String getUserId() {
        new GetUserIdAsyncTask(authDao).execute();
        return userId;
    }

    /**
     * Creates an entry of a user's login state in the database
     * This is cleared when the user logs out.
     * At a time, the table only contains one entry
     */
    private static class AddAuthInfoAsyncTask extends AsyncTask<Auth, Void, Void> {

        private AuthDao authDao;
        private AddAuthInfoAsyncTask(AuthDao authDao) {
            this.authDao = authDao;
        }

        @Override
        protected Void doInBackground(Auth... auths) {
            authDao.insertAuthInfo(auths[0]);
            return null;
        }
    }

    /**
     * Get auth information
     */
    private static class GetAuthInfoAsyncTask extends AsyncTask<Void, Void, Void> {

        private AuthDao authDao;
        private GetAuthInfoAsyncTask(AuthDao authDao) {
            this.authDao = authDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            authInfo = authDao.getAuthInfo();
            return null;
        }
    }

    /**
     * Updates the access token for the given user
     */
    private static class UpdateAccessTokenAsyncTask extends AsyncTask<String, Void, Void> {

        private AuthDao authDao;
        private UpdateAccessTokenAsyncTask(AuthDao authDao) {
            this.authDao = authDao;
        }

        @Override
        protected Void doInBackground(String... tokens) {
            authDao.updateAccessToken(tokens[0]);
            return null;
        }
    }

    /**
     * Clears the login state
     */
    private class ClearAuthInfoAsyncTask extends AsyncTask<Void, Void, Void> {

        private AuthDao authDao;
        private ClearAuthInfoAsyncTask(AuthDao authDao) {
            this.authDao = authDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            authDao.clearTokens();
            return null;
        }
    }

    /**
     * Get access token set in the database
     */
    private class GetAccessTokenAsyncTask extends AsyncTask<Void, Void, String> {

        private AuthDao authDao;

        private GetAccessTokenAsyncTask(AuthDao authDao) {
            this.authDao = authDao;
        }

        @Override
        protected String doInBackground(Void... voids) {
            accessToken = authDao.getAccessToken();
            return accessToken;
        }
    }

    /**
     * Get refresh token from the database
     */
    private class GetRefreshTokenAsyncTask extends AsyncTask<Void, Void, Void> {

        private AuthDao authDao;
        private GetRefreshTokenAsyncTask(AuthDao authDao) {
            this.authDao = authDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            refreshToken = authDao.getRefreshToken();
            return null;
        }
    }

    /**
     * Get refresh token from the database
     */
    private class GetUserIdAsyncTask extends AsyncTask<Void, Void, Void> {

        private AuthDao authDao;
        private GetUserIdAsyncTask(AuthDao authDao) {
            this.authDao = authDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userId = authDao.getUserId();
            return null;
        }
    }
}
