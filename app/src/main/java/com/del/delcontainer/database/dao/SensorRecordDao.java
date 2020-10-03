package com.del.delcontainer.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.del.delcontainer.database.entities.Heart;
import com.del.delcontainer.database.entities.SensorRecord;

import java.util.Date;
import java.util.List;

@Dao
public interface SensorRecordDao {

    @Insert
    void insert(SensorRecord sensorRecord);

    @Query("SELECT * FROM sensor_records WHERE sensor=:sensor ORDER BY id ASC")
    List<SensorRecord> getAllSensorRecords(String sensor);

    @Query("SELECT * from sensor_records WHERE sensor= :sensor ORDER BY date DESC LIMIT 1")
    SensorRecord getLatestSensorRecord(String sensor);

    @Query("SELECT * FROM sensor_records WHERE sensor= :sensor AND date BETWEEN :from AND :to")
    List<SensorRecord> getSensorRecordsBetweenDates(String sensor, Date from, Date to);

    @Query("DELETE FROM sensor_records")
    void deleteAllSensorRecords();
}
