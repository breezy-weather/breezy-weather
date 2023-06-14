package wangdaye.com.geometricweather.common.basic.models.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

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

    public Alert(long alertId, Date startDate, Date endDate,
                 String description, String content,
                 String type, int priority) {
        this.alertId = alertId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.content = content;
        this.type = type;
        this.priority = priority;
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
