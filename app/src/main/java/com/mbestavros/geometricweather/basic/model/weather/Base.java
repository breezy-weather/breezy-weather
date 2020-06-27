package com.mbestavros.geometricweather.basic.model.weather;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mbestavros.geometricweather.utils.manager.TimeManager;

/**
 * Base.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class Base implements Serializable {

    private String cityId;
    private long timeStamp;

    private Date publishDate; // device time.
    private long publishTime; // device time.

    private Date updateDate; // device time.
    private long updateTime; // device time.

    public Base(String cityId, long timeStamp,
                Date publishDate, long publishTime,
                Date updateDate, long updateTime) {
        this.cityId = cityId;
        this.timeStamp = timeStamp;
        this.publishDate = publishDate;
        this.publishTime = publishTime;
        this.updateDate = updateDate;
        this.updateTime = updateTime;
    }

    public String getCityId() {
        return cityId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTime(Context c, Date date) {
        if (TimeManager.is12Hour(c)) {
            return new SimpleDateFormat("h:mm aa").format(date);
        } else {
            return new SimpleDateFormat("HH:mm").format(date);
        }
    }
}
