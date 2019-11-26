package com.del.delcontainer.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.del.delcontainer.database.entities.Heart;

import java.util.List;

@Dao
public interface HeartDao {

    @Insert
    void insert(Heart heart);

    @Query("SELECT * FROM heart ORDER BY id ASC")
    LiveData<List<Heart>> getAllLiveHeartRateInfo();

    @Query("SELECT * FROM heart")
    List<Heart> getAllHeartRateInfo();

    @Query("SELECT * from heart ORDER BY id DESC LIMIT 1")
    Heart getLatestHeartData();

    @Query("DELETE FROM heart")
    void deleteAllHeartRateInfo();
}
