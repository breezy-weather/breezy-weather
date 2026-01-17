/*
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

package org.breezyweather.ui.theme.resource.providers

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import breezyweather.domain.weather.reference.WeatherCode
import org.breezyweather.R
import org.breezyweather.ui.theme.resource.utils.Constants

class ChronusResourceProvider(
    c: Context,
    pkgName: String,
    private val mDefaultProvider: ResourceProvider,
) : ResourceProvider() {
    private lateinit var mContext: Context
    override var providerName: String? = null
    private var mIconDrawable: Drawable? = null

    init {
        try {
            mContext = c.createPackageContext(
                pkgName,
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )
            val manager = mContext.packageManager
            val info = manager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
            providerName = manager.getApplicationLabel(info).toString()
            mIconDrawable = mContext.applicationInfo.loadIcon(mContext.packageManager)
        } catch (e: Exception) {
            buildDefaultInstance(c)
        }
    }

    private fun buildDefaultInstance(c: Context) {
        mContext = c.applicationContext
        providerName = c.getString(R.string.breezy_weather)
        mIconDrawable = mDefaultProvider.providerIcon
    }

    override val packageName: String
        get() = mContext.packageName
    override val providerIcon: Drawable
        get() = mIconDrawable ?: getWeatherIcon(WeatherCode.CLEAR, true)

    // weather icon.
    override fun getWeatherIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            return getDrawable(getWeatherIconName(code, dayTime))!!
        } catch (ignore: Exception) { }
        return mDefaultProvider.getWeatherIcon(code, dayTime)
    }

    override fun getWeatherIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        val resName = getWeatherIconName(code, dayTime)
        val resId = getResId(mContext, resName, "drawable")
        return if (resId != 0) {
            getDrawableUri(resName)
        } else {
            mDefaultProvider.getWeatherIconUri(code, dayTime)
        }
    }

    override fun getWeatherIcons(code: WeatherCode?, dayTime: Boolean): Array<Drawable?> {
        return arrayOf(getWeatherIcon(code, dayTime), null, null)
    }

    private fun getDrawable(resName: String): Drawable? {
        return try {
            ResourcesCompat.getDrawable(
                mContext.resources,
                getResId(mContext, resName, "drawable"),
                null
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getWeatherIconName(code: WeatherCode?, daytime: Boolean): String {
        return "weather" + Constants.SEPARATOR + getWeatherIconCode(code, daytime)
    }

    private fun getWeatherIconCode(code: WeatherCode?, daytime: Boolean): String {
        return when (code) {
            WeatherCode.CLEAR -> if (daytime) "32" else "31"
            WeatherCode.PARTLY_CLOUDY -> if (daytime) "30" else "29"
            WeatherCode.CLOUDY -> "26"
            WeatherCode.RAIN -> "12"
            WeatherCode.SNOW -> "16"
            WeatherCode.WIND -> "24"
            WeatherCode.FOG -> "20"
            WeatherCode.HAZE -> "21"
            WeatherCode.SLEET -> "5"
            WeatherCode.HAIL -> "17"
            WeatherCode.THUNDER -> "4"
            WeatherCode.THUNDERSTORM -> "4"
            else -> "na"
        }
    }

    // animator.
    override fun getWeatherAnimators(code: WeatherCode?, dayTime: Boolean): Array<Animator?> {
        return arrayOf(null, null, null)
    }

    // minimal icon.
    override fun getMinimalLightIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return mDefaultProvider.getMinimalLightIcon(code, dayTime)
    }

    override fun getMinimalLightIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        return mDefaultProvider.getMinimalLightIconUri(code, dayTime)
    }

    override fun getMinimalGreyIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return mDefaultProvider.getMinimalGreyIcon(code, dayTime)
    }

    override fun getMinimalGreyIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        return mDefaultProvider.getMinimalGreyIconUri(code, dayTime)
    }

    override fun getMinimalDarkIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return mDefaultProvider.getMinimalDarkIcon(code, dayTime)
    }

    override fun getMinimalDarkIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        return mDefaultProvider.getMinimalDarkIconUri(code, dayTime)
    }

    override fun getMinimalXmlIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return mDefaultProvider.getMinimalXmlIcon(code, dayTime)
    }

    override fun getMinimalIcon(code: WeatherCode?, dayTime: Boolean): Icon {
        return mDefaultProvider.getMinimalIcon(code, dayTime)
    }

    // shortcut.
    override fun getShortcutsIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return mDefaultProvider.getShortcutsIcon(code, dayTime)
    }

    override fun getShortcutsForegroundIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return mDefaultProvider.getShortcutsForegroundIcon(code, dayTime)
    }

    override val sunDrawable: Drawable
        get() = getWeatherIcon(WeatherCode.CLEAR, true)
    override val moonDrawable: Drawable
        get() = getWeatherIcon(WeatherCode.CLEAR, false)

    companion object {
        fun getProviderList(
            context: Context,
            defaultProvider: ResourceProvider,
        ): List<ChronusResourceProvider> {
            val providerList = mutableListOf<ChronusResourceProvider>()
            val infoList = context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Constants.CATEGORY_CHRONUS_ICON_PACK),
                PackageManager.GET_RESOLVED_FILTER
            )
            for (info in infoList) {
                providerList.add(
                    ChronusResourceProvider(
                        context,
                        info.activityInfo.applicationInfo.packageName,
                        defaultProvider
                    )
                )
            }
            return providerList
        }

        fun isChronusIconProvider(context: Context, packageName: String): Boolean {
            val infoList = context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Constants.CATEGORY_CHRONUS_ICON_PACK),
                PackageManager.GET_RESOLVED_FILTER
            )
            for (info in infoList) {
                if (packageName == info.activityInfo.applicationInfo.packageName) {
                    return true
                }
            }
            return false
        }
    }
}
