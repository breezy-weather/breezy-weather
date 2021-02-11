package wangdaye.com.geometricweather.main;

import android.content.Context;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class MainModuleUtils {

    public static boolean isMultiFragmentEnabled(Context context) {
        return DisplayUtils.isLandscape(context);
    }

    public static boolean needUpdate(Context context, Location location) {
        float pollingIntervalInHour = SettingsOptionManager.getInstance(context)
                .getUpdateInterval()
                .getIntervalInHour();
        return !location.isUsable()
                || location.getWeather() == null
                || !location.getWeather().isValid(pollingIntervalInHour);
    }
}
