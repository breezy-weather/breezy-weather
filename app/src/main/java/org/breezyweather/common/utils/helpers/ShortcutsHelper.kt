/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider
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
            val list = Location.excludeInvalidResidentLocation(context, locationList)
            val provider = ResourcesProviderFactory.newInstance
            val shortcutList: MutableList<ShortcutInfo> = ArrayList()

            // location list.
            val count = min(shortcutManager.maxShortcutCountPerActivity - 1, list.size)
            for (i in 0 until count) {
                val weather = WeatherEntityRepository.readWeather(list[i])
                val icon =
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
                val title = list[i].getPlace(context, true)
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
                ResourceHelper.getShortcutsForegroundIcon(provider, code, daytime)
            )
        )
    }

    private fun getIcon(provider: ResourceProvider, code: WeatherCode, daytime: Boolean): Icon {
        return Icon.createWithBitmap(
            drawableToBitmap(
                ResourceHelper.getShortcutsIcon(provider, code, daytime)
            )
        )
    }
}
