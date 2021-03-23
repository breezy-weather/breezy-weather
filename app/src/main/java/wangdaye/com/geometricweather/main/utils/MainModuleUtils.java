package wangdaye.com.geometricweather.main.utils;

import android.content.Context;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class MainModuleUtils {

    public static boolean needUpdate(Context context, Location location) {
        float pollingIntervalInHour = SettingsOptionManager.getInstance(context)
                .getUpdateInterval()
                .getIntervalInHour();
        return !location.isUsable()
                || location.getWeather() == null
                || !location.getWeather().isValid(pollingIntervalInHour);
    }
}
