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

package org.breezyweather.theme.resource.providers

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.core.content.res.ResourcesCompat
import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.R
import org.breezyweather.theme.resource.utils.Config
import org.breezyweather.theme.resource.utils.Constants
import org.breezyweather.theme.resource.utils.XmlHelper

open class IconPackResourcesProvider(
    c: Context,
    pkgName: String,
    private val mDefaultProvider: ResourceProvider,
) : ResourceProvider() {
    private lateinit var mContext: Context
    override var providerName: String? = null
    private var mIconDrawable: Drawable? = null
    private lateinit var mConfig: Config
    private var mDrawableFilter: Map<String, String>? = null
    private var mAnimatorFilter: Map<String, String>? = null
    private var mShortcutFilter: Map<String, String>? = null
    private var mSunMoonFilter: Map<String, String>? = null

    init {
        try {
            mContext = c.createPackageContext(pkgName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
            val manager = mContext.packageManager
            val info = manager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
            providerName = manager.getApplicationLabel(info).toString()
            mIconDrawable = mContext.applicationInfo.loadIcon(mContext.packageManager)
            val res = mContext.resources
            var resId = getMetaDataResource(Constants.META_DATA_PROVIDER_CONFIG)
            mConfig = if (resId != 0) {
                XmlHelper.getConfig(res.getXml(resId))
            } else {
                val geometricResId =
                    getMetaDataResource(Constants.GEOMETRIC_META_DATA_PROVIDER_CONFIG)
                if (geometricResId != 0) {
                    XmlHelper.getConfig(res.getXml(geometricResId))
                } else {
                    Config()
                }
            }
            resId = getMetaDataResource(Constants.META_DATA_DRAWABLE_FILTER)
            mDrawableFilter = if (resId != 0) {
                XmlHelper.getFilterMap(res.getXml(resId))
            } else {
                val geometricResId =
                    getMetaDataResource(Constants.GEOMETRIC_META_DATA_DRAWABLE_FILTER)
                if (geometricResId != 0) {
                    XmlHelper.getFilterMap(res.getXml(geometricResId))
                } else {
                    HashMap()
                }
            }
            resId = getMetaDataResource(Constants.META_DATA_ANIMATOR_FILTER)
            mAnimatorFilter = if (resId != 0) {
                XmlHelper.getFilterMap(res.getXml(resId))
            } else {
                val geometricResId =
                    getMetaDataResource(Constants.GEOMETRIC_META_DATA_ANIMATOR_FILTER)
                if (geometricResId != 0) {
                    XmlHelper.getFilterMap(res.getXml(geometricResId))
                } else {
                    HashMap()
                }
            }
            resId = getMetaDataResource(Constants.META_DATA_SHORTCUT_FILTER)
            mShortcutFilter = if (resId != 0) {
                XmlHelper.getFilterMap(res.getXml(resId))
            } else {
                val geometricResId =
                    getMetaDataResource(Constants.GEOMETRIC_META_DATA_SHORTCUT_FILTER)
                if (geometricResId != 0) {
                    XmlHelper.getFilterMap(res.getXml(geometricResId))
                } else {
                    HashMap()
                }
            }
            resId = getMetaDataResource(Constants.META_DATA_SUN_MOON_FILTER)
            mSunMoonFilter = if (resId != 0) {
                XmlHelper.getFilterMap(res.getXml(resId))
            } else {
                val geometricResId =
                    getMetaDataResource(Constants.GEOMETRIC_META_DATA_SUN_MOON_FILTER)
                if (geometricResId != 0) {
                    XmlHelper.getFilterMap(res.getXml(geometricResId))
                } else {
                    HashMap()
                }
            }
        } catch (e: Exception) {
            buildDefaultInstance(c)
        }
    }

    private fun buildDefaultInstance(c: Context) {
        mContext = c.applicationContext
        providerName = c.getString(R.string.breezy_weather)
        mIconDrawable = mDefaultProvider.providerIcon
        val res = mContext.resources
        try {
            mConfig = XmlHelper.getConfig(res.getXml(R.xml.icon_provider_config))
            mDrawableFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_drawable_filter))
            mAnimatorFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_animator_filter))
            mShortcutFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_shortcut_filter))
            mSunMoonFilter = XmlHelper.getFilterMap(res.getXml(R.xml.icon_provider_sun_moon_filter))
        } catch (e: Exception) {
            mConfig = Config()
            mDrawableFilter = HashMap()
            mAnimatorFilter = HashMap()
            mShortcutFilter = HashMap()
            mSunMoonFilter = HashMap()
        }
    }

    private fun getMetaDataResource(key: String): Int {
        return try {
            mContext.packageManager.getApplicationInfo(
                mContext.packageName,
                PackageManager.GET_META_DATA
            ).metaData.getInt(key)
        } catch (e: Exception) {
            0
        }
    }

    override val packageName: String
        get() = mContext.packageName
    override val providerIcon: Drawable
        get() = mIconDrawable ?: getWeatherIcon(WeatherCode.CLEAR, true)

    // weather icon.
    override fun getWeatherIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            if (mConfig.hasWeatherIcons) {
                return getDrawable(getWeatherIconName(code, dayTime))!!
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getWeatherIcon(code, dayTime)
    }

    override fun getWeatherIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        if (mConfig.hasWeatherIcons) {
            val resName = getWeatherIconName(code, dayTime)
            val resId: Int = getResId(mContext, resName, "drawable")
            if (resId != 0) {
                return getDrawableUri(resName)
            }
        }
        return mDefaultProvider.getWeatherIconUri(code, dayTime)
    }

    @Size(3)
    override fun getWeatherIcons(code: WeatherCode?, dayTime: Boolean): Array<Drawable?> {
        return if (mConfig.hasWeatherIcons) {
            if (mConfig.hasWeatherAnimators) {
                arrayOf(
                    getDrawable(getWeatherIconName(code, dayTime, 1)),
                    getDrawable(getWeatherIconName(code, dayTime, 2)),
                    getDrawable(getWeatherIconName(code, dayTime, 3))
                )
            } else {
                arrayOf(getWeatherIcon(code, dayTime), null, null)
            }
        } else {
            mDefaultProvider.getWeatherIcons(code, dayTime)
        }
    }

    private fun getDrawable(resName: String?): Drawable? {
        if (resName == null) return null
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

    open fun getWeatherIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetWeatherIconName(code, daytime)
        )
    }

    open fun getWeatherIconName(
        code: WeatherCode?,
        daytime: Boolean,
        @IntRange(from = 1, to = 3) index: Int,
    ): String? {
        return getFilterResource(
            mDrawableFilter,
            innerGetWeatherIconName(code, daytime) + Constants.SEPARATOR + index
        )
    }

    // animator.
    @Size(3)
    override fun getWeatherAnimators(code: WeatherCode?, dayTime: Boolean): Array<Animator?> {
        return if (mConfig.hasWeatherIcons) {
            if (mConfig.hasWeatherAnimators) {
                arrayOf(
                    getAnimator(getWeatherAnimatorName(code, dayTime, 1)),
                    getAnimator(getWeatherAnimatorName(code, dayTime, 2)),
                    getAnimator(getWeatherAnimatorName(code, dayTime, 3))
                )
            } else {
                arrayOf(null, null, null)
            }
        } else {
            mDefaultProvider.getWeatherAnimators(code, dayTime)
        }
    }

    private fun getAnimator(resName: String?): Animator? {
        if (resName == null) return null
        return try {
            AnimatorInflater.loadAnimator(
                mContext,
                getResId(mContext, resName, "animator")
            )
        } catch (e: Exception) {
            null
        }
    }

    open fun getWeatherAnimatorName(
        code: WeatherCode?,
        daytime: Boolean,
        @IntRange(from = 1, to = 3) index: Int,
    ): String? {
        return getFilterResource(
            mAnimatorFilter,
            innerGetWeatherAnimatorName(code, daytime) + Constants.SEPARATOR + index
        )
    }

    // minimal icon.
    override fun getMinimalLightIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            if (mConfig.hasMinimalIcons) {
                return getDrawable(getMiniLightIconName(code, dayTime))!!
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getMinimalLightIcon(code, dayTime)
    }

    override fun getMinimalLightIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        if (mConfig.hasMinimalIcons) {
            val resName = getMiniLightIconName(code, dayTime)
            val resId: Int = getResId(mContext, resName, "drawable")
            if (resId != 0) {
                return getDrawableUri(resName)
            }
        }
        return mDefaultProvider.getMinimalLightIconUri(code, dayTime)
    }

    override fun getMinimalGreyIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            if (mConfig.hasMinimalIcons) {
                return getDrawable(getMiniGreyIconName(code, dayTime))!!
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getMinimalGreyIcon(code, dayTime)
    }

    override fun getMinimalGreyIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        if (mConfig.hasMinimalIcons) {
            val resName = getMiniGreyIconName(code, dayTime)
            val resId: Int = getResId(mContext, resName, "drawable")
            if (resId != 0) {
                return getDrawableUri(resName)
            }
        }
        return mDefaultProvider.getMinimalGreyIconUri(code, dayTime)
    }

    override fun getMinimalDarkIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            if (mConfig.hasMinimalIcons) {
                return getDrawable(getMiniDarkIconName(code, dayTime))!!
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getMinimalDarkIcon(code, dayTime)
    }

    override fun getMinimalDarkIconUri(code: WeatherCode?, dayTime: Boolean): Uri {
        if (mConfig.hasMinimalIcons) {
            val resName = getMiniDarkIconName(code, dayTime)
            val resId: Int = getResId(mContext, resName, "drawable")
            if (resId != 0) {
                return getDrawableUri(resName)
            }
        }
        return mDefaultProvider.getMinimalDarkIconUri(code, dayTime)
    }

    override fun getMinimalXmlIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            if (mConfig.hasMinimalIcons) {
                return getDrawable(getMiniXmlIconName(code, dayTime))!!
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getMinimalXmlIcon(code, dayTime)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun getMinimalIcon(code: WeatherCode?, dayTime: Boolean): Icon {
        try {
            if (mConfig.hasMinimalIcons) {
                return Icon.createWithResource(
                    mContext,
                    getResId(mContext, getMiniXmlIconName(code, dayTime), "drawable")
                )
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getMinimalIcon(code, dayTime)
    }

    fun getMiniLightIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.LIGHT
        )
    }

    fun getMiniGreyIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.GREY
        )
    }

    fun getMiniDarkIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.DARK
        )
    }

    fun getMiniXmlIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mDrawableFilter,
            innerGetMiniIconName(code, daytime) + Constants.SEPARATOR + Constants.XML
        )
    }

    // shortcut.
    override fun getShortcutsIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            if (mConfig.hasShortcutIcons) {
                return getDrawable(getShortcutsIconName(code, dayTime))!!
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getShortcutsIcon(code, dayTime)
    }

    override fun getShortcutsForegroundIcon(code: WeatherCode?, dayTime: Boolean): Drawable {
        try {
            if (mConfig.hasShortcutIcons) {
                return getDrawable(getShortcutsForegroundIconName(code, dayTime))!!
            }
        } catch (ignore: Exception) {
        }
        return mDefaultProvider.getShortcutsForegroundIcon(code, dayTime)
    }

    fun getShortcutsIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mShortcutFilter,
            innerGetShortcutsIconName(code, daytime)
        )
    }

    fun getShortcutsForegroundIconName(code: WeatherCode?, daytime: Boolean): String {
        return getFilterResource(
            mShortcutFilter,
            innerGetShortcutsIconName(code, daytime) + Constants.SEPARATOR + Constants.FOREGROUND
        )
    }

    override val sunDrawable: Drawable
        // sun and moon.
        get() = if (mConfig.hasSunMoonDrawables) {
            try {
                getReflectDrawable(sunDrawableClassName)!!
            } catch (e: Exception) {
                getWeatherIcon(WeatherCode.CLEAR, true)
            }
        } else {
            mDefaultProvider.sunDrawable
        }
    override val moonDrawable: Drawable
        get() = if (mConfig.hasSunMoonDrawables) {
            try {
                getReflectDrawable(moonDrawableClassName)!!
            } catch (e: Exception) {
                getWeatherIcon(WeatherCode.CLEAR, false)
            }
        } else {
            mDefaultProvider.moonDrawable
        }

    private fun getReflectDrawable(className: String?): Drawable? {
        return try {
            val clazz = mContext.classLoader.loadClass(className)
            clazz.newInstance() as Drawable
        } catch (e: Exception) {
            null
        }
    }

    open val sunDrawableClassName: String?
        get() = mSunMoonFilter?.getOrElse(Constants.RESOURCES_SUN) { null }
    open val moonDrawableClassName: String?
        get() = mSunMoonFilter?.getOrElse(Constants.RESOURCES_MOON) { null }

    companion object {
        fun getProviderList(
            context: Context,
            defaultProvider: ResourceProvider,
        ): List<IconPackResourcesProvider> {
            val providerList = mutableListOf<IconPackResourcesProvider>()
            val infoList = context.packageManager.queryIntentActivities(
                Intent(Constants.ACTION_ICON_PROVIDER),
                PackageManager.GET_RESOLVED_FILTER
            )
            for (info in infoList) {
                providerList.add(
                    IconPackResourcesProvider(
                        context,
                        info.activityInfo.applicationInfo.packageName,
                        defaultProvider
                    )
                )
            }
            val geometricInfoList = context.packageManager.queryIntentActivities(
                Intent(Constants.GEOMETRIC_ACTION_ICON_PROVIDER),
                PackageManager.GET_RESOLVED_FILTER
            )
            for (info in geometricInfoList) {
                providerList.add(
                    IconPackResourcesProvider(
                        context,
                        info.activityInfo.applicationInfo.packageName,
                        defaultProvider
                    )
                )
            }
            return providerList
        }

        fun isIconPackIconProvider(context: Context, packageName: String): Boolean {
            val infoList = context.packageManager.queryIntentActivities(
                Intent(Constants.ACTION_ICON_PROVIDER),
                PackageManager.GET_RESOLVED_FILTER
            )
            for (info in infoList) {
                if (packageName == info.activityInfo.applicationInfo.packageName) {
                    return true
                }
            }
            val geometricInfoList = context.packageManager.queryIntentActivities(
                Intent(Constants.GEOMETRIC_ACTION_ICON_PROVIDER),
                PackageManager.GET_RESOLVED_FILTER
            )
            for (info in geometricInfoList) {
                if (packageName == info.activityInfo.applicationInfo.packageName) {
                    return true
                }
            }
            return false
        }

        private fun getFilterResource(filter: Map<String, String>?, key: String): String {
            return filter?.getOrElse(key) { null } ?: key
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
            return innerGetWeatherIconName(code, daytime) + Constants.SEPARATOR + Constants.MINI
        }

        private fun innerGetShortcutsIconName(code: WeatherCode?, daytime: Boolean): String {
            return Constants.getShortcutsName(code) +
                Constants.SEPARATOR +
                if (daytime) Constants.DAY else Constants.NIGHT
        }
    }
}
