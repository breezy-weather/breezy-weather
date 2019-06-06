package wangdaye.com.geometricweather.utils.manager;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Shortcuts utils.
 * */

@RequiresApi(api = Build.VERSION_CODES.N_MR1)
public class ShortcutsManager {

    public static void refreshShortcutsInNewThread(final Context c, List<Location> locationList) {
        final List<Location> list = new ArrayList<>(locationList);

        ThreadManager.getInstance().execute(() -> {
            ShortcutManager shortcutManager = c.getSystemService(ShortcutManager.class);
            if (shortcutManager == null) {
                return;
            }

            ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

            List<ShortcutInfo> shortcutList = new ArrayList<>();

            // refresh button.
            Icon icon;
            String title = c.getString(R.string.refresh);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                icon = Icon.createWithAdaptiveBitmap(
                        drawableToBitmap(
                                Objects.requireNonNull(
                                        ContextCompat.getDrawable(c, R.drawable.shortcuts_refresh_foreground)
                                )
                        )
                );
            } else {
                icon = Icon.createWithResource(c, R.drawable.shortcuts_refresh);
            }
            shortcutList.add(
                    new ShortcutInfo.Builder(c, "refresh_data")
                            .setIcon(icon)
                            .setShortLabel(title)
                            .setLongLabel(title)
                            .setIntent(IntentHelper.buildAwakeUpdateActivityIntent())
                            .build()
            );

            // location list.
            Weather weather;
            for (int i = 0; i < list.size(); i ++) {
                weather = DatabaseHelper.getInstance(c).readWeather(list.get(i));
                if (weather != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        icon = getAdaptiveIcon(
                                provider, weather.realTime.weatherKind, TimeManager.isDaylight(weather)
                        );
                    } else {
                        icon = getIcon(
                                provider, weather.realTime.weatherKind, TimeManager.isDaylight(weather)
                        );
                    }
                } else {
                    icon = getIcon(provider, Weather.KIND_CLEAR, true);
                }

                title = list.get(i).isLocal() ? c.getString(R.string.local) : list.get(i).getCityName(c);

                shortcutList.add(
                        new ShortcutInfo.Builder(c, list.get(i).cityId)
                                .setIcon(icon)
                                .setShortLabel(title)
                                .setLongLabel(title)
                                .setIntent(IntentHelper.buildMainActivityIntent(c, list.get(i)))
                                .build()
                );
            }

            try {
                shortcutManager.setDynamicShortcuts(shortcutList);
            } catch (Exception ignore) {
                // do nothing.
            }
        });
    }

    @NonNull
    private static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    private static Icon getAdaptiveIcon(ResourceProvider helper, String weatherKind, boolean daytime) {
        return Icon.createWithAdaptiveBitmap(
                drawableToBitmap(
                        WeatherHelper.getShortcutsForegroundIcon(helper, weatherKind, daytime)
                )
        );
    }

    @NonNull
    private static Icon getIcon(ResourceProvider helper, String weatherKind, boolean daytime) {
        return Icon.createWithBitmap(
                drawableToBitmap(
                        WeatherHelper.getShortcutsIcon(helper, weatherKind, daytime)
                )
        );
    }
}
