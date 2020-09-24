package com.del.delcontainer.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.del.delcontainer.utils.Converters;

import java.util.Date;

@Entity(tableName = "sensor_records")
public class SensorRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "sensor")
    private String sensor;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "reading")
    private String reading;

    public SensorRecord(Date date, String sensor, String reading) {
        this.date = date;
        this.sensor = sensor;
        this.reading = reading;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getReading() {
        return reading;
    }

    public String getSensor() { return sensor; }

    public void setSensor(String sensor) { this.sensor = sensor; }
}
