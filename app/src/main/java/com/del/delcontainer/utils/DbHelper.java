package com.del.delcontainer.utils;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.del.delcontainer.database.DelDatabase;

public class DbHelper {

    private static final String TAG = "DbHelper";

    private static DbHelper dbHelper;
    private DelDatabase delDatabase = null;
    private Context context;

    private DbHelper() {
        ;
    }

    public static DbHelper getInstance() {
        if(null == dbHelper) {
            dbHelper = new DbHelper();
        }

        return dbHelper;
    }

    public void buildDatabase() {

        if(null == delDatabase) {
            delDatabase = Room.databaseBuilder(this.context,
                    DelDatabase.class, "del_database")
                    .allowMainThreadQueries() // This is to be removed
                    .build();

            Log.d(TAG, "buildDatabase: initializing database.");
        }
    }

    public DelDatabase getDelDatabase() {

        if(null == delDatabase) {
            buildDatabase();
        }

        return delDatabase;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
