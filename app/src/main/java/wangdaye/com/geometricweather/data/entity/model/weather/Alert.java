package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuAlertResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntity;

/**
 * Alert.
 * */

public class Alert implements Parcelable {

    public int id;
    public String description;
    public String content;
    public String publishTime;
    
/*
    Alert buildAlert(FWResult.Alarms alarm) {
        description = alarm.alarmDesc;
        content = alarm.alarmContent;
        publishTime = alarm.publishTime;
        return this;
    }
*/
    public Alert buildAlert(Context c, AccuAlertResult result) {
        id = result.AlertID;
        description = result.Description.Localized;
        content = result.Area.get(0).Text;
        publishTime = c.getString(R.string.publish_at) + " " + result.Area.get(0).StartTime.split("T")[0]
                + " " + result.Area.get(0).StartTime.split("T")[1].split(":")[0]
                + ":" + result.Area.get(0).StartTime.split("T")[1].split(":")[1];
        return this;
    }

    public Alert buildAlert(Context c, CNWeatherResult.Alert alert) {
        try {
            String[] dates = alert.pubTime.split(" ")[0].split("-");
            String[] times = alert.pubTime.split(" ")[1].split(":");
            id = Integer.parseInt(alert.alarmPic2 + alert.alarmPic1 + dates[2] + times[0]);
        } catch (Exception e) {
            id = 0;
        }
        description = alert.alarmTp1 + alert.alarmTp2 + c.getString(R.string.action_alert);
        content = alert.content;
        publishTime = c.getString(R.string.publish_at) + " " + alert.pubTime;
        return this;
    }

    Alert buildAlert(AlarmEntity entity) {
        id = entity.alertId;
        description = entity.description;
        content = entity.content;
        publishTime = entity.publishTime;
        return this;
    }

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