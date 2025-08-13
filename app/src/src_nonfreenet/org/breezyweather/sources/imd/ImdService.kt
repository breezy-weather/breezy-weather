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

package org.breezyweather.sources.imd

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.imd.json.ImdWeatherResult
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours

class ImdService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "imd"
    override val name = "IMD (${context.currentLocale.getCountryName("IN")})"
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client
            .baseUrl(IMD_BASE_URL)
            .build()
            .create(ImdApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to "India Meteorological Department"
    )
    override val attributionLinks = mapOf(
        "India Meteorological Department" to "https://imd.gov.in/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("IN", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        // Forecast data is obtained from https://mausamgram.imd.gov.in/
        // It computes forecasts on a 0.125° × 0.125° grid.
        // We must feed the coordinates at exactly this resolution, or the data retrieval will fail.
        val latitude = location.latitude.div(0.125).roundToInt().times(0.125)
        val longitude = location.longitude.div(0.125).roundToInt().times(0.125)

        // Forecast data is computed with UTC as the base time.
        val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val timestampRegex = Regex("""\d{10}""")

        // IMD computes 3 sets of forecast data for its grid: 1-hourly, 3-hourly, 6-hourly.
        // However, they are not computed at a regular basis.
        // Also, each set is computed separately from each other.
        // Therefore, we must first poll a text file which tells us the last time a particular forecast is updated.
        // Then we grab the forecast with the corresponding "date" parameter.
        // If one particular forecast grab does not return valid output, don't fail the whole process,
        // because the other two forecasts may still yield valid output that can be used.
        val requests = List(IMD_TIMEFRAMES.size) {
            Request.Builder().url(IMD_BASE_URL + "mmem_" + IMD_TIMEFRAMES[it] + ".txt").build()
        }
        val timestamps = MutableList(IMD_TIMEFRAMES.size) { "" }
        val forecasts = MutableList(IMD_TIMEFRAMES.size) {
            okHttpClient.newCall(requests[it]).execute().use { call ->
                if (call.isSuccessful) {
                    timestamps[it] = call.body.string().substringBefore(',')
                    if (timestampRegex.matches(timestamps[it])) {
                        mApi.getForecast(
                            lat = latitude,
                            lon = longitude,
                            date = timestamps[it] + "_" + IMD_TIMEFRAMES[it] + "_0p125"
                        ).onErrorResumeNext {
                            Observable.just(ImdWeatherResult())
                        }
                    } else {
                        Observable.just(ImdWeatherResult())
                    }
                } else {
                    Observable.just(ImdWeatherResult())
                }
            }
        }

        return Observable.zip(forecasts[0], forecasts[1], forecasts[2]) {
                forecast1hr: ImdWeatherResult,
                forecast3hr: ImdWeatherResult,
                forecast6hr: ImdWeatherResult,
            ->
            val hourlyForecast = getHourlyForecast(
                forecast1hr,
                forecast3hr,
                forecast6hr,
                if (timestampRegex.matches(timestamps[0])) {
                    formatter.parse(timestamps[0])!!.time
                } else {
                    null
                },
                if (timestampRegex.matches(timestamps[1])) {
                    formatter.parse(timestamps[1])!!.time
                } else {
                    null
                },
                if (timestampRegex.matches(timestamps[2])) {
                    formatter.parse(timestamps[2])!!.time
                } else {
                    null
                }
            )

            WeatherWrapper(
                dailyForecast = getDailyForecast(location, hourlyForecast),
                hourlyForecast = hourlyForecast
            )
        }
    }

    private fun getDailyForecast(
        location: Location,
        hourlyForecast: List<HourlyWrapper>,
    ): List<DailyWrapper> {
        // Need to provide an empty daily list so that
        // CommonConverter.kt will compute the daily forecast items.
        val dates = hourlyForecast.groupBy { it.date.getIsoFormattedDate(location) }.keys
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        formatter.timeZone = location.timeZone
        val now = Calendar.getInstance(location.timeZone)
        now.add(Calendar.DATE, -1)
        val yesterday = formatter.format(now.time)
        val dailyList = mutableListOf<DailyWrapper>()
        dates.forEachIndexed { i, day ->
            // Do not add days prior to yesterday.
            // Do not add the last day to avoid incomplete day.
            if (day >= yesterday && i < (dates.size - 1)) {
                dailyList.add(
                    DailyWrapper(
                        date = formatter.parse(day)!!
                    )
                )
            }
        }
        return dailyList
    }

    private fun getHourlyForecast(
        forecast1hr: ImdWeatherResult,
        forecast3hr: ImdWeatherResult,
        forecast6hr: ImdWeatherResult,
        forecast1hrTimestamp: Long?,
        forecast3hrTimestamp: Long?,
        forecast6hrTimestamp: Long?,
    ): List<HourlyWrapper> {
        val hourlyList = mutableListOf<HourlyWrapper>()
        val apcpMap = mutableMapOf<Long, Double?>()
        val tempMap = mutableMapOf<Long, Double?>()
        val wspdMap = mutableMapOf<Long, Double?>()
        val wdirMap = mutableMapOf<Long, Double?>()
        val rhMap = mutableMapOf<Long, Double?>()
        val tcdcMap = mutableMapOf<Long, Double?>()
        val gustMap = mutableMapOf<Long, Double?>()
        var key: Long

        val forecastSets = listOf(forecast6hr, forecast3hr, forecast1hr)
        val forecastParameters = listOf(
            mapOf(
                "timestamp" to forecast6hrTimestamp,
                "interval" to 6.hours.inWholeMilliseconds,
                "size" to 40 // 10 days
            ),
            mapOf(
                "timestamp" to forecast3hrTimestamp,
                "interval" to 3.hours.inWholeMilliseconds,
                "size" to 40 // 5 days
            ),
            mapOf(
                "timestamp" to forecast1hrTimestamp,
                "interval" to 1.hours.inWholeMilliseconds,
                "size" to 36 // 1.5 days
            )
        )

        // Put data from all three forecasts into respective maps
        // first 6 hr, then 3 hr, then 1 hr
        forecastSets.forEachIndexed { set, forecast ->
            forecastParameters.getOrNull(set)?.let { parameters ->
                if (parameters["timestamp"] != null) {
                    forecast.let {
                        // skip 0 as it is always "NaN"
                        for (i in 1..parameters["size"]!!.toInt()) {
                            key = parameters["timestamp"]!! + (parameters["interval"]!! * i)
                            apcpMap[key] = it.apcp?.getOrNull(i) as Double?
                            tempMap[key] = it.temp?.getOrNull(i) as Double?
                            wspdMap[key] = it.wspd?.getOrNull(i) as Double?
                            wdirMap[key] = it.wdir?.getOrNull(i) as Double?
                            rhMap[key] = it.rh?.getOrNull(i) as Double?
                            tcdcMap[key] = it.tcdc?.getOrNull(i) as Double?
                            gustMap[key] = it.gust?.getOrNull(i) as Double?
                        }
                    }
                }
            }
        }

        val keys = apcpMap.keys.sorted()
        keys.forEach {
            hourlyList.add(
                HourlyWrapper(
                    date = Date(it),
                    temperature = TemperatureWrapper(
                        temperature = tempMap[it]
                    ),
                    precipitation = Precipitation(
                        total = apcpMap[it]?.millimeters
                    ),
                    wind = Wind(
                        degree = wdirMap[it],
                        speed = wspdMap[it],
                        gusts = gustMap[it]
                    ),
                    relativeHumidity = rhMap[it],
                    cloudCover = tcdcMap[it]?.toInt()
                )
            )
        }
        return hourlyList
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val IMD_BASE_URL = "https://mausamgram.imd.gov.in/"
        private val IMD_TIMEFRAMES = arrayOf("1hr", "3hr", "6hr")
    }
}
