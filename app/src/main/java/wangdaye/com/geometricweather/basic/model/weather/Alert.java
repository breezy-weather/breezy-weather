package wangdaye.com.geometricweather.basic.model.weather;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Alert.
 * */

public class Alert implements Parcelable {

    public int id;
    public String description;
    public String content;
    public String publishTime;

    public Alert(int id, String description, String content, String publishTime) {
        this.id = id;
        this.description = description;
        this.content = content;
        this.publishTime = publishTime;
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