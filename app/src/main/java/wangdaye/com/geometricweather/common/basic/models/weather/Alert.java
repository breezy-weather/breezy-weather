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
    private final Date startDate;
    private final Date endDate;

    private final String description;
    private final String content;

    private final String type;
    private final int priority;
    @ColorInt private final int color;

    public Alert(long alertId, Date startDate, Date endDate,
                 String description, String content,
                 String type, int priority, int color) {
        this.alertId = alertId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.content = content;
        this.type = type;
        this.priority = priority;
        this.color = color; // TODO: Not used?
    }

    public long getAlertId() {
        return alertId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
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

    // parcelable.

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.alertId);
        dest.writeLong(this.startDate != null ? this.startDate.getTime() : -1);
        dest.writeLong(this.endDate != null ? this.endDate.getTime() : -1);
        dest.writeString(this.description);
        dest.writeString(this.content);
        dest.writeString(this.type);
        dest.writeInt(this.priority);
        dest.writeInt(this.color);
    }

    protected Alert(Parcel in) {
        this.alertId = in.readLong();
        long tmpStartDate = in.readLong();
        this.startDate = tmpStartDate == -1 ? null : new Date(tmpStartDate);
        long tmpEndDate = in.readLong();
        this.endDate = tmpEndDate == -1 ? null : new Date(tmpEndDate);
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
