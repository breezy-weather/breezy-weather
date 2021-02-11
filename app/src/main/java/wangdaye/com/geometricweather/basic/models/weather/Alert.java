package wangdaye.com.geometricweather.basic.models.weather;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Alert.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class Alert implements Parcelable, Serializable {

    private final long alertId;
    private final Date date;
    private final long time;

    private final String description;
    private final String content;

    private final String type;
    private final int priority;
    @ColorInt private final int color;

    public Alert(long alertId, Date date, long time,
                 String description, String content,
                 String type, int priority, int color) {
        this.alertId = alertId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.content = content;
        this.type = type;
        this.priority = priority;
        this.color = color;
    }

    public long getAlertId() {
        return alertId;
    }

    public Date getDate() {
        return date;
    }

    public long getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public int getColor() {
        return color;
    }

    public static void deduplication(List<Alert> alertList) {
        for (int i = 0; i < alertList.size(); i ++) {
            String type = alertList.get(i).getType();
            for (int j = alertList.size() - 1; j > i; j --) {
                if (alertList.get(j).getType().equals(type)) {
                    alertList.remove(j);
                }
            }
        }
    }

    public static void descByTime(List<Alert> alertList) {
        for (int i = 0; i < alertList.size(); i ++) {
            long maxTime = alertList.get(i).time;
            int maxIndex = i;
            for (int j = i + 1; j < alertList.size(); j ++) {
                if (maxTime < alertList.get(j).time) {
                    maxTime = alertList.get(j).time;
                    maxIndex = j;
                }
            }
            if (maxIndex != i) {
                Collections.swap(alertList, maxIndex, i);
            }
        }
    }

    // parcelable.

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.alertId);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeLong(this.time);
        dest.writeString(this.description);
        dest.writeString(this.content);
        dest.writeString(this.type);
        dest.writeInt(this.priority);
        dest.writeInt(this.color);
    }

    protected Alert(Parcel in) {
        this.alertId = in.readLong();
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        this.time = in.readLong();
        this.description = in.readString();
        this.content = in.readString();
        this.type = in.readString();
        this.priority = in.readInt();
        this.color = in.readInt();
    }

    public static final Creator<Alert> CREATOR = new Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel source) {
            return new Alert(source);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };
}
