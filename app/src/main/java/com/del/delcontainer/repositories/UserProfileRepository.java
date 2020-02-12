package com.del.delcontainer.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.del.delcontainer.database.DelDatabase;
import com.del.delcontainer.database.dao.UserProfileDao;
import com.del.delcontainer.database.entities.UserProfile;

public class UserProfileRepository {

    private UserProfileDao userProfileDao;
    private LiveData<UserProfile> userProfile;

    public UserProfileRepository(Application application) {

        // pass in application as the instance reference as it is a subclass of Context
        DelDatabase database = DelDatabase.getInstance(application);

        userProfileDao = database.userProfileDao();
        userProfile = userProfileDao.getUserProfile();
    }

    /**
     * Methods to handle userProfile operations
     * @param userProfile
     */
    public void createProfile(UserProfile userProfile) {
        new CreateProfileAsyncTask(userProfileDao).execute(userProfile);
    }

    public void updateProfile(UserProfile userProfile) {
        new UpdateProfileAsyncTask(userProfileDao).execute(userProfile);
    }

    public void deleteProfile(UserProfile userProfile) {
        new DeleteProfileAsyncTask(userProfileDao).execute(userProfile);
    }

    public LiveData<UserProfile> getProfile() {
        return userProfile;
    }


    /**
     * As RoomDatabase doesn't allow DB operations in the foreground,
     * we need to create AsyncTasks for all ops
     */
    private static class CreateProfileAsyncTask extends AsyncTask<UserProfile, Void, Void> {

        private UserProfileDao userProfileDao;
        private CreateProfileAsyncTask(UserProfileDao userProfileDao) {
            this.userProfileDao = userProfileDao;
        }

        @Override
        protected Void doInBackground(UserProfile... userProfiles) {
            userProfileDao.createUser(userProfiles[0]);
            return null;
        }
    }

    private static class UpdateProfileAsyncTask extends AsyncTask<UserProfile, Void, Void> {

        private UserProfileDao userProfileDao;
        private UpdateProfileAsyncTask(UserProfileDao userProfileDao) {
            this.userProfileDao = userProfileDao;
        }

        @Override
        protected Void doInBackground(UserProfile... userProfiles) {
            userProfileDao.updateUserDetails(userProfiles[0]);
            return null;
        }
    }

    private static class DeleteProfileAsyncTask extends AsyncTask<UserProfile, Void, Void> {

        private UserProfileDao userProfileDao;
        private DeleteProfileAsyncTask(UserProfileDao userProfileDao) {
            this.userProfileDao = userProfileDao;
        }

        @Override
        protected Void doInBackground(UserProfile... userProfiles) {
            userProfileDao.deleteUserProfile(userProfiles[0]);
            return null;
        }
    }
}
