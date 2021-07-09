package wangdaye.com.geometricweather.search

import android.content.Context
import android.text.TextUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.settings.ConfigStore
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.weather.WeatherHelper
import javax.inject.Inject

class SearchActivityRepository constructor(context: Context,
                                           private val weatherHelper: WeatherHelper,
                                           private val ioDispatcher: CoroutineDispatcher) {

    companion object {
        private const val PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG"
        private const val KEY_DISABLED_SOURCES = "DISABLED_SOURCES"
        private const val KEY_LAST_DEFAULT_SOURCE = "LAST_DEFAULT_SOURCE"
        private const val DEFAULT_DISABLED_SOURCES_VALUE = "ENABLE_DEFAULT_SOURCE_ONLY"
    }

    private val config: ConfigStore = ConfigStore.getInstance(context, PREFERENCE_SEARCH_CONFIG)
    private var validSourceCache: List<WeatherSource>? = null
    private var lastDefaultSourceCache: WeatherSource? = null

    @Inject constructor(@ApplicationContext context: Context,
                        weatherHelper: WeatherHelper): this(context, weatherHelper, Dispatchers.IO)

    suspend fun searchLocationList(context: Context,
                                   query: String,
                                   enabledSources: List<WeatherSource>): List<Location> {
        return withContext(ioDispatcher) {
            weatherHelper.getLocation(context, query, enabledSources)
        }.result ?: ArrayList()
    }

    fun getValidWeatherSources(context: Context): List<WeatherSource> {
        val defaultSource = SettingsManager.getInstance(context).getWeatherSource()
        if (validSourceCache != null && defaultSource == lastDefaultSourceCache) {
            return validSourceCache!!
        }

        val totals = WeatherSource.ACCU.declaringClass.enumConstants ?: emptyArray()

        val lastDefaultSource = config.getString(KEY_LAST_DEFAULT_SOURCE, "")
        lastDefaultSourceCache = WeatherSource.getInstance(lastDefaultSource)

        val value: String?
        if (defaultSource.sourceId != lastDefaultSource) {
            // last default source is not equal to current default source which is set by user.

            // we need reset the value.
            value = DEFAULT_DISABLED_SOURCES_VALUE
            config.edit()
                    .putString(KEY_DISABLED_SOURCES, value)
                    .putString(KEY_LAST_DEFAULT_SOURCE, defaultSource.sourceId)
                    .apply()
        } else {
            value = config.getString(KEY_DISABLED_SOURCES, "")
            config.edit()
                    .putString(KEY_LAST_DEFAULT_SOURCE, defaultSource.sourceId)
                    .apply()
        }

        if (TextUtils.isEmpty(value)) {
            return totals.asList()
        }

        if (value == DEFAULT_DISABLED_SOURCES_VALUE) {
            return listOf(defaultSource)
        }

        val idList = value!!.split(Regex(","))
        val invalids = Array(idList.size) {
            WeatherSource.getInstance(idList[it])
        }

        val validList = ArrayList<WeatherSource>()
        val invalidList: List<WeatherSource> = invalids.asList()
        for (source in totals) {
            if (!invalidList.contains(source)) {
                validList.add(source)
            }
        }

        validSourceCache = validList
        return validList
    }

    fun setValidWeatherSources(validList: List<WeatherSource>) {
        validSourceCache = validList
        val totals = WeatherSource.ACCU.declaringClass.enumConstants ?: return

        val b = StringBuilder()
        for (source in totals) {
            if (!validList.contains(source)) {
                b.append(",").append(source.sourceId)
            }
        }

        val value = if (b.isNotEmpty()) {
            b.substring(1)
        } else {
            ""
        }

        config.edit()
                .putString(KEY_DISABLED_SOURCES, value)
                .apply()
    }
}