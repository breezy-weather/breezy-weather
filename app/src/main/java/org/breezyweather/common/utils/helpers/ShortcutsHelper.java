package org.breezyweather.common.utils.helpers;

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

import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.basic.models.weather.WeatherCode;
import org.breezyweather.db.repositories.WeatherEntityRepository;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.ResourcesProviderFactory;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;

/**
 * Shortcuts manager.
 * */

@RequiresApi(api = Build.VERSION_CODES.N_MR1)
public class ShortcutsHelper {

    public static void refreshShortcutsInNewThread(final Context context, List<Location> locationList) {
        AsyncHelper.runOnIO(() -> {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if (shortcutManager == null) {
                return;
            }

            List<Location> list = Location.excludeInvalidResidentLocation(context, locationList);
            ResourceProvider provider = ResourcesProviderFactory.getNewInstance();
            List<ShortcutInfo> shortcutList = new ArrayList<>();

            // refresh button.
            Icon icon;
            String title = context.getString(R.string.action_refresh);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                icon = Icon.createWithAdaptiveBitmap(
                        drawableToBitmap(
                                Objects.requireNonNull(
                                        ContextCompat.getDrawable(context, R.drawable.shortcuts_refresh_foreground)
                                )
                        )
                );
            } else {
                icon = Icon.createWithResource(context, R.drawable.shortcuts_refresh);
            }
            shortcutList.add(
                    new ShortcutInfo.Builder(context, "refresh_data")
                            .setIcon(icon)
                            .setShortLabel(title)
                            .setLongLabel(title)
                            .setIntent(IntentHelper.buildAwakeUpdateActivityIntent())
                            .build()
            );

            // location list.
            int count = Math.min(
                    shortcutManager.getMaxShortcutCountPerActivity() - 1,
                    list.size()
            );
            for (int i = 0; i < count; i++) {
                Weather weather = WeatherEntityRepository.INSTANCE.readWeather(list.get(i));
                if (weather != null && weather.getCurrent() != null && weather.getCurrent().getWeatherCode() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        icon = getAdaptiveIcon(
                                provider,
                                weather.getCurrent().getWeatherCode(),
                                list.get(i).isDaylight()
                        );
                    } else {
                        icon = getIcon(
                                provider,
                                weather.getCurrent().getWeatherCode(),
                                list.get(i).isDaylight()
                        );
                    }
                } else {
                    icon = getIcon(provider, WeatherCode.CLEAR, true);
                }

                title = list.get(i).isCurrentPosition() ? context.getString(R.string.location_current) : list.get(i).getCityName(context);

                shortcutList.add(
                        new ShortcutInfo.Builder(context, list.get(i).getFormattedId())
                                .setIcon(icon)
                                .setShortLabel(title)
                                .setLongLabel(title)
                                .setIntent(IntentHelper.buildMainActivityIntent(list.get(i)))
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
    private static Icon getAdaptiveIcon(ResourceProvider provider, WeatherCode code, boolean daytime) {
        return Icon.createWithAdaptiveBitmap(
                drawableToBitmap(
                        ResourceHelper.getShortcutsForegroundIcon(provider, code, daytime)
                )
        );
    }

    @NonNull
    private static Icon getIcon(ResourceProvider provider, WeatherCode code, boolean daytime) {
        return Icon.createWithBitmap(
                drawableToBitmap(
                        ResourceHelper.getShortcutsIcon(provider, code, daytime)
                )
        );
    }
}
