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

package org.breezyweather.ui.theme.resource.providers

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.core.content.res.ResourcesCompat
import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.ui.common.images.MoonDrawable
import org.breezyweather.ui.common.images.SunDrawable
import org.breezyweather.ui.theme.resource.utils.Constants
import org.breezyweather.ui.theme.resource.utils.XmlHelper
import java.util.Objects

class DefaultResourceProvider : ResourceProvider() {
    private val mContext: Context
    override var providerName: String? = null
    override val providerIcon: Drawable?
    private var mDrawableFilter: Map<String, String>? = null
    private var mAnimatorFilter: Map<String, String>? = null
    private var mShortcutFilter: Map<String, String>? = null

    init {
        mContext = BreezyWeather.instance
        providerName = mContext.getString(R.string.breezy_weather)
        providerIcon = mContext.applicationInfo.loadIcon(mContext.packageManager)
        val res = mContext.resources
        try {
            mDrawableFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_drawable_filter))
            mAnimatorFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_animator_filter))
            mShortcutFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_shortcut_filter))
        } catch (e: Exception) {
            mDrawableFilter = HashMap()
            mAnimatorFilter = HashMap()
            mShortcutFilter = HashMap()
        }
    }

    override val packageName: String
        get() = mContext.packageName

    // weather icon.
    override fun getWeatherIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return getDrawable(getWeatherIconName(code, dayTime))!!
    }

    override fun getWeatherIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        return getDrawableUri(getWeatherIconName(code, dayTime))
    }

    @Size(3)
    override fun getWeatherIcons(code: WeatherCode?, dayTime: Boolean): Array<Drawable?> {
        return arrayOf(
            getDrawable(getWeatherIconName(code, dayTime, 1)),
            getDrawable(getWeatherIconName(code, dayTime, 2)),
            getDrawable(getWeatherIconName(code, dayTime, 3))
        )
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

    private fun getWeatherIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetWeatherIconName(code, daytime)
        )
    }

    private fun getWeatherIconName(
        code: WeatherCode?,
        daytime: Boolean,
        @IntRange(from = 1, to = 3) index: Int,
    ): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetWeatherIconName(code, daytime) + Constants.SEPARATOR + index
        )
    }

    // animator.
    @Size(3)
    override fun getWeatherAnimators(code: WeatherCode?, dayTime: Boolean): Array<Animator?> {
        return arrayOf(
            getAnimator(getWeatherAnimatorName(code, dayTime, 1)),
            getAnimator(getWeatherAnimatorName(code, dayTime, 2)),
            getAnimator(getWeatherAnimatorName(code, dayTime, 3))
        )
    }

    private fun getAnimator(resName: String): Animator? {
        return try {
            AnimatorInflater.loadAnimator(
                mContext,
                getResId(mContext, resName, "animator")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getWeatherAnimatorName(
        code: WeatherCode?,
        daytime: Boolean,
        @IntRange(from = 1, to = 3) index: Int,
    ): String {
        return getFilterResource(
            mAnimatorFilter,
            innerGetWeatherAnimatorName(code, daytime) + Constants.SEPARATOR + index
        )
    }

    // minimal.
    override fun getMinimalLightIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return getDrawable(getMiniLightIconName(code, dayTime))!!
    }

    override fun getMinimalLightIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        return Objects.requireNonNull(
            getDrawableUri(getMiniLightIconName(code, dayTime))
        )
    }

    override fun getMinimalGreyIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return getDrawable(getMiniGreyIconName(code, dayTime))!!
    }

    override fun getMinimalGreyIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        return Objects.requireNonNull(
            getDrawableUri(getMiniGreyIconName(code, dayTime))
        )
    }

    override fun getMinimalDarkIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return getDrawable(getMiniDarkIconName(code, dayTime))!!
    }

    override fun getMinimalDarkIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        return Objects.requireNonNull(
            getDrawableUri(getMiniDarkIconName(code, dayTime))
        )
    }

    override fun getMinimalXmlIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return getDrawable(getMiniXmlIconName(code, dayTime))!!
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun getMinimalIcon(code: WeatherCode?, dayTime: Boolean): Icon {
        return Objects.requireNonNull(
            Icon.createWithResource(
                mContext,
                getMinimalXmlIconId(code, dayTime)
            )
        )
    }

    @DrawableRes
    fun getMinimalXmlIconId(code: WeatherCode?, dayTime: Boolean): Int {
        return getResId(mContext, getMiniXmlIconName(code, dayTime), "drawable")
    }

    private fun getMiniLightIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.LIGHT
        )
    }

    private fun getMiniGreyIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.GREY
        )
    }

    private fun getMiniDarkIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.DARK
        )
    }

    private fun getMiniXmlIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.XML
        )
    }

    // shortcut.
    override fun getShortcutsIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return getDrawable(getShortcutsIconName(code, dayTime))!!
    }

    override fun getShortcutsForegroundIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        return getDrawable(getShortcutsForegroundIconName(code, dayTime))!!
    }

    private fun getShortcutsIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mShortcutFilter,
            innerGetShortcutsIconName(code, daytime)
        )
    }

    private fun getShortcutsForegroundIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mShortcutFilter,
            getShortcutsIconName(code, daytime) + Constants.SEPARATOR + Constants.FOREGROUND
        )
    }

    override val sunDrawable: Drawable
        // sun and moon.
        get() = SunDrawable()
    override val moonDrawable: Drawable
        get() = MoonDrawable()

    companion object {
        fun isDefaultIconProvider(packageName: String): Boolean {
            return packageName == BreezyWeather.instance.packageName
        }

        private fun getFilterResource(filter: Map<String, String>?, key: String): String {
            val value = filter?.getOrElse(key) { null }
            return if (value.isNullOrEmpty()) key else value
        }

        private fun innerGetWeatherIconName(code: WeatherCode?, daytime: Boolean): String {
            return Constants.getResourcesName(code) +
                Constants.SEPARATOR +
                if (daytime) Constants.DAY else Constants.NIGHT
        }

        private fun innerGetWeatherAnimatorName(code: WeatherCode?, daytime: Boolean): String {
            return Constants.getResourcesName(code) +
                Constants.SEPARATOR +
                if (daytime) Constants.DAY else Constants.NIGHT
        }

        private fun innerGetMiniIconName(code: WeatherCode?, daytime: Boolean): String {
            return innerGetWeatherIconName(code, daytime) +
                Constants.SEPARATOR +
                Constants.MINI
        }

        private fun innerGetShortcutsIconName(code: WeatherCode?, daytime: Boolean): String {
            return Constants.getShortcutsName(code) +
                Constants.SEPARATOR +
                if (daytime) Constants.DAY else Constants.NIGHT
        }
    }
}
