package wangdaye.com.geometricweather.main.utils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

public class MainPalette implements Parcelable {

    public final @Size(3) @ColorInt int[] themeColors;
    public final @ColorInt int weatherBackgroundColor;
    public final @ColorInt int headerTextColor;
    public final @ColorInt int accentColor;
    public final @ColorInt int rootColor;
    public final @ColorInt int lineColor;
    public final @ColorInt int titleColor;
    public final @ColorInt int contentColor;
    public final @ColorInt int subtitleColor;

    public MainPalette(Context context, MainThemeManager manager) {
        themeColors = manager.getWeatherThemeColors();
        weatherBackgroundColor = manager.getWeatherBackgroundColor();
        headerTextColor = manager.getHeaderTextColor(context);
        accentColor = manager.getAccentColor(context);
        rootColor = manager.getRootColor(context);
        lineColor = manager.getLineColor(context);
        titleColor = manager.getTextTitleColor(context);
        contentColor = manager.getTextContentColor(context);
        subtitleColor = manager.getTextSubtitleColor(context);
    }

    protected MainPalette(Parcel in) {
        themeColors = in.createIntArray();
        weatherBackgroundColor = in.readInt();
        headerTextColor = in.readInt();
        accentColor = in.readInt();
        rootColor = in.readInt();
        lineColor = in.readInt();
        titleColor = in.readInt();
        contentColor = in.readInt();
        subtitleColor = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(themeColors);
        dest.writeInt(weatherBackgroundColor);
        dest.writeInt(headerTextColor);
        dest.writeInt(accentColor);
        dest.writeInt(rootColor);
        dest.writeInt(lineColor);
        dest.writeInt(titleColor);
        dest.writeInt(contentColor);
        dest.writeInt(subtitleColor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MainPalette> CREATOR = new Creator<MainPalette>() {
        @Override
        public MainPalette createFromParcel(Parcel in) {
            return new MainPalette(in);
        }

        @Override
        public MainPalette[] newArray(int size) {
            return new MainPalette[size];
        }
    };
}
