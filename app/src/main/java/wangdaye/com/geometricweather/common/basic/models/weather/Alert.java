package wangdaye.com.geometricweather.common.basic.models.weather;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<String> typeSet = new HashSet<>();

        for (int i = alertList.size() - 1; i >= 0; i --) {
            Alert alert = alertList.get(i);

            if (typeSet.contains(alert.getType())) {
                alertList.remove(i);
            } else {
                typeSet.add(alert.type);
            }
        }
    }

    public static void descByTime(List<Alert> alertList) {
        Collections.sort(
                alertList,
                (o1, o2) -> (int) (o2.getTime() - o1.getTime())
        );
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
