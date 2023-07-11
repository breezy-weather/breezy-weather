package org.breezyweather.search

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.ConfigStore
import org.breezyweather.weather.WeatherHelper
import javax.inject.Inject

class SearchActivityRepository @Inject internal constructor(
    @ApplicationContext context: Context,
    private val mWeatherHelper: WeatherHelper
) {
    private val mConfig: ConfigStore = ConfigStore(context, PREFERENCE_SEARCH_CONFIG)

    fun searchLocationList(
        context: Context, query: String, enabledSource: WeatherSource,
        callback: (t: Pair<List<Location>?, RequestErrorType?>?, done: Boolean) -> Unit
    ) {
        mWeatherHelper.requestSearchLocations(
            context, query, enabledSource,
            object : WeatherHelper.OnRequestLocationListener {
                override fun requestLocationSuccess(query: String, locationList: List<Location>) {
                    callback(Pair<List<Location>, RequestErrorType?>(locationList, null), true)
                }

                override fun requestLocationFailed(query: String, requestErrorType: RequestErrorType) {
                    callback(Pair<List<Location>?, RequestErrorType>(null, requestErrorType), true)
                }
            })
    }

    var lastSelectedWeatherSource: WeatherSource
        get() {
            val lastDefaultSource = mConfig.getString(KEY_LAST_DEFAULT_SOURCE, "")
            return WeatherSource.getInstance(lastDefaultSource)
        }
        set(weatherSource) {
            mConfig.edit().putString(KEY_LAST_DEFAULT_SOURCE, weatherSource.id).apply()
        }

    fun cancel() {
        mWeatherHelper.cancel()
    }

    companion object {
        private const val PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG"
        private const val KEY_LAST_DEFAULT_SOURCE = "LAST_DEFAULT_SOURCE"
    }
}
