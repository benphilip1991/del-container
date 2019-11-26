package com.del.delcontainer.repositories;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.del.delcontainer.database.DelDatabase;
import com.del.delcontainer.database.dao.HeartDao;
import com.del.delcontainer.database.entities.Heart;

import java.util.List;

public class HeartRateRepository {

    private static HeartRateRepository instance;
    private static HeartDao heartDao;
    private static LiveData<List<Heart>> liveHeartData;
    private static List<Heart> heartData;
    private static Heart latestHeartData;

    private HeartRateRepository() {;}

    public static synchronized HeartRateRepository getInstance(Context context) {

        if(null == instance) {
            instance = new HeartRateRepository();
            DelDatabase database = DelDatabase.getInstance(context);
            heartDao = database.heartDao();
            liveHeartData = heartDao.getAllLiveHeartRateInfo();
        }
        return instance;
    }

    /**
     * Methods to handle userProfile operations
     * @param heart
     */
    public void addHeartRateInfo(Heart heart) {

        new CreateHRDataAsyncTask(heartDao).execute(heart);
    }

    public void deleteAllHeartRateInfo() {

        new DeleteHRDataAsyncTask(heartDao).execute();
    }

    public LiveData<List<Heart>> getLiveHeartData() {
        return liveHeartData;
    }

    public List<Heart> getHeartData() {
        new GetHeartRateDataAsyncTask(heartDao).execute();
        return heartData;
    }

    public Heart getLatestHeartData() {
        new GetLatestHeartRateDataAsyncTask(heartDao).execute();
        return latestHeartData;
    }


    /**
     * As RoomDatabase doesn't allow DB operations in the foreground,
     * we need to create AsyncTasks for all ops
     */
    private static class CreateHRDataAsyncTask extends AsyncTask<Heart, Void, Void> {

        private HeartDao heartDao;
        private CreateHRDataAsyncTask(HeartDao heartDao) {
            this.heartDao = heartDao;
        }

        @Override
        protected Void doInBackground(Heart... heart) {
            heartDao.insert(heart[0]);
            return null;
        }
    }

    private static class DeleteHRDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private HeartDao heartDao;
        private DeleteHRDataAsyncTask(HeartDao heartDao) {
            this.heartDao = heartDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            heartDao.deleteAllHeartRateInfo();
            return null;
        }
    }

    // Param, Progress, Return
    private static class GetHeartRateDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private HeartDao heartDao;
        private GetHeartRateDataAsyncTask(HeartDao heartDao) {
            this.heartDao = heartDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            heartData = heartDao.getAllHeartRateInfo();
            return null;
        }
    }

    private static class GetLatestHeartRateDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private HeartDao heartDao;
        private GetLatestHeartRateDataAsyncTask(HeartDao heartDao) {
            this.heartDao = heartDao;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            latestHeartData = heartDao.getLatestHeartData();
            return null;
        }
    }
}
