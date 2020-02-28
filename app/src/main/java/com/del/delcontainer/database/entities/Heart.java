package com.del.delcontainer.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "heart")
public class Heart {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String date;

    @ColumnInfo(name = "heart_rate")
    private int heartRate;

    public Heart(String date, int heartRate) {
        this.date = date;
        this.heartRate = heartRate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getHeartRate() {
        return heartRate;
    }
}
