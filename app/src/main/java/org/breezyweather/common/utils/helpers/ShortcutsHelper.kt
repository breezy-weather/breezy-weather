package org.breezyweather.common.utils.helpers

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.Location.Companion.excludeInvalidResidentLocation
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.db.repositories.WeatherEntityRepository.readWeather
import org.breezyweather.theme.resource.ResourceHelper.getShortcutsForegroundIcon
import org.breezyweather.theme.resource.ResourceHelper.getShortcutsIcon
import org.breezyweather.theme.resource.ResourcesProviderFactory.newInstance
import org.breezyweather.theme.resource.providers.ResourceProvider
import java.util.Objects
import kotlin.math.min

/**
 * Shortcuts manager.
 */
@RequiresApi(api = Build.VERSION_CODES.N_MR1)
object ShortcutsHelper {
    fun refreshShortcutsInNewThread(context: Context, locationList: List<Location>) {
        AsyncHelper.runOnIO {
            val shortcutManager = context.getSystemService<ShortcutManager>(
                ShortcutManager::class.java
            ) ?: return@runOnIO
            val list = excludeInvalidResidentLocation(context, locationList)
            val provider = newInstance
            val shortcutList: MutableList<ShortcutInfo> = ArrayList()

            // refresh button.
            var icon: Icon
            var title = context.getString(R.string.action_refresh)
            icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Icon.createWithAdaptiveBitmap(
                    drawableToBitmap(
                        AppCompatResources.getDrawable(context, R.drawable.shortcuts_refresh_foreground)!!
                    )
                )
            } else {
                Icon.createWithResource(context, R.drawable.shortcuts_refresh)
            }
            shortcutList.add(
                ShortcutInfo.Builder(context, "refresh_data")
                    .setIcon(icon)
                    .setShortLabel(title)
                    .setLongLabel(title)
                    .setIntent(IntentHelper.buildAwakeUpdateActivityIntent())
                    .build()
            )

            // location list.
            val count = min(shortcutManager.maxShortcutCountPerActivity - 1, list.size)
            for (i in 0 until count) {
                val weather = readWeather(list[i])
                icon =
                    if (weather?.current?.weatherCode != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getAdaptiveIcon(
                                provider,
                                weather.current.weatherCode,
                                list[i].isDaylight
                            )
                        } else {
                            getIcon(
                                provider,
                                weather.current.weatherCode,
                                list[i].isDaylight
                            )
                        }
                    } else {
                        getIcon(provider, WeatherCode.CLEAR, true)
                    }
                title = if (list[i].isCurrentPosition) {
                    context.getString(R.string.location_current)
                } else list[i].getCityName(context)
                shortcutList.add(
                    ShortcutInfo.Builder(context, list[i].formattedId)
                        .setIcon(icon)
                        .setShortLabel(title)
                        .setLongLabel(title)
                        .setIntent(IntentHelper.buildMainActivityIntent(list[i]))
                        .build()
                )
            }
            try {
                shortcutManager.setDynamicShortcuts(shortcutList)
            } catch (ignore: Exception) {
                // do nothing.
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getAdaptiveIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        daytime: Boolean
    ): Icon {
        return Icon.createWithAdaptiveBitmap(
            drawableToBitmap(
                getShortcutsForegroundIcon(provider, code, daytime)
            )
        )
    }

    private fun getIcon(provider: ResourceProvider, code: WeatherCode, daytime: Boolean): Icon {
        return Icon.createWithBitmap(
            drawableToBitmap(
                getShortcutsIcon(provider, code, daytime)
            )
        )
    }
}
