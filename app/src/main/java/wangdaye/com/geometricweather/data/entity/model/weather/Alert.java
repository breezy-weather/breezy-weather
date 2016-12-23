package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.result.neww.NewAlertResult;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntity;

/**
 * Alert.
 * */

public class Alert implements Parcelable {
    // data
    public int id;
    public String description;
    public String content;
    public String publishTime;
    
    /** <br> life cycle. */
/*
    Alert buildAlert(FWResult.Alarms alarm) {
        description = alarm.alarmDesc;
        content = alarm.alarmContent;
        publishTime = alarm.publishTime;
        return this;
    }
*/
    public Alert buildAlert(Context c, NewAlertResult result) {
        id = result.AlertID;
        description = result.Description.Localized;
        content = result.Area.get(0).Text;
        publishTime = c.getString(R.string.publish_at) + " " + result.Area.get(0).StartTime.split("T")[0]
                + " " + result.Area.get(0).StartTime.split("T")[1].split(":")[0] + ":" + result.Area.get(0).StartTime.split("T")[1].split(":")[1];
        return this;
    }

    Alert buildAlert(AlarmEntity entity) {
        id = entity.alertId;
        description = entity.description;
        content = entity.content;
        publishTime = entity.publishTime;
        return this;
    }

    /** <br> parcelable. */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.description);
        dest.writeString(this.content);
        dest.writeString(this.publishTime);
    }

    public Alert() {
    }

    protected Alert(Parcel in) {
        this.id = in.readInt();
        this.description = in.readString();
        this.content = in.readString();
        this.publishTime = in.readString();
    }

    public static final Parcelable.Creator<Alert> CREATOR = new Parcelable.Creator<Alert>() {
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