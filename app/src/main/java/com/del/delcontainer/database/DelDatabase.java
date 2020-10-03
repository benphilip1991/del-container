package com.del.delcontainer.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.del.delcontainer.database.dao.AuthDao;
import com.del.delcontainer.database.dao.HeartDao;
import com.del.delcontainer.database.dao.SensorRecordDao;
import com.del.delcontainer.database.dao.UserProfileDao;
import com.del.delcontainer.database.entities.Auth;
import com.del.delcontainer.database.entities.Heart;
import com.del.delcontainer.database.entities.SensorRecord;
import com.del.delcontainer.database.entities.UserProfile;
import com.del.delcontainer.utils.Converters;

@Database(entities = {UserProfile.class, Heart.class, Auth.class, SensorRecord.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class DelDatabase extends RoomDatabase {

    private static DelDatabase instance;

    public abstract UserProfileDao userProfileDao();
    public abstract HeartDao heartDao();
    public abstract AuthDao authDao();
    public abstract SensorRecordDao sensorRecordDao();

    public static synchronized DelDatabase getInstance(Context context) {
        if(null == instance) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    DelDatabase.class, "del_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(onCreateCallback)
                    .build();
        }

        return instance;
    }

    // Called the first time the app is run
    private static RoomDatabase.Callback onCreateCallback = new RoomDatabase.Callback() {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            new PopulateDbAsyncTask(instance).execute();
            super.onCreate(db);
        }
    };


    /**
     * Need an async task for populating the database
     */
    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {

        private UserProfileDao userProfileDao;
        private PopulateDbAsyncTask(DelDatabase db) {
            userProfileDao = db.userProfileDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userProfileDao.createUser(new UserProfile("12345", "John", "Doe", 20, "M"));

            return null;
        }
    }
}
