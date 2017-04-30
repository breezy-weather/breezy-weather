package wangdaye.com.geometricweather.utils.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Shortcuts utils.
 * */

public class ShortcutsManager {

    @TargetApi(25)
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static void refreshShortcuts(final Context c, List<Location> locationList) {
        final List<Location> list = new ArrayList<>(locationList.size());
        list.addAll(locationList);

        ThreadManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ShortcutManager shortcutManager = c.getSystemService(ShortcutManager.class);
                List<ShortcutInfo> shortcutList = new ArrayList<>();
                for (int i = 0; i < list.size(); i ++) {
                    Icon icon;
                    Weather weather = DatabaseHelper.getInstance(c).readWeather(list.get(i));
                    if (weather != null) {
                        icon = Icon.createWithResource(
                                c,
                                WeatherHelper.getShortcutIcon(
                                        weather.realTime.weatherKind, TimeManager.getInstance(c).isDayTime()));
                    } else {
                        icon = Icon.createWithResource(c, R.drawable.ic_shortcut_sun_day);
                    }

                    String title = list.get(i).isLocal() ? c.getString(R.string.local) : list.get(i).city;

                    shortcutList.add(
                            new ShortcutInfo.Builder(c, list.get(i).cityId)
                                    .setIcon(icon)
                                    .setShortLabel(title)
                                    .setLongLabel(title)
                                    .setIntent(IntentHelper.buildMainActivityIntent(c, list.get(i)))
                                    .build());
                }

                shortcutManager.setDynamicShortcuts(shortcutList);
            }
        });
    }
}
