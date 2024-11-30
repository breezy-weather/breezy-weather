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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.imd.json.ImdWeatherResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt

class ImdService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource {

    override val id = "imd"
    override val name = "India Meteorological Department"
    override val privacyPolicyUrl = ""

    override val color = Color.rgb(10, 88, 202)
    override val weatherAttribution = "India Meteorological Department"

    private val mApi by lazy {
        client
            .baseUrl(IMD_BASE_URL)
            .build()
            .create(ImdApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    override val supportedFeaturesInMain = listOf<SecondaryWeatherSourceFeature>()

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?,
    ): Boolean {
        return location.countryCode.equals("IN", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>,
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
                    timestamps[it] = call.body!!.string().substringBefore(',')
                    if (timestampRegex.matches(timestamps[it])) {
                        mApi.getForecast(
                            lat = latitude,
                            lon = longitude,
                            date = timestamps[it] + "_" + IMD_TIMEFRAMES[it] + "_0p125"
                        ).onErrorResumeNext {
                            // TODO: Log warning
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
                forecast1hrResult: ImdWeatherResult,
                forecast3hrResult: ImdWeatherResult,
                forecast6hrResult: ImdWeatherResult,
            ->
            convert(
                location = location,
                forecast1hr = forecast1hrResult,
                forecast3hr = forecast3hrResult,
                forecast6hr = forecast6hrResult,
                forecast1hrTimestamp = if (timestampRegex.matches(timestamps[0])) {
                    formatter.parse(timestamps[0])!!.time
                } else {
                    null
                },
                forecast3hrTimestamp = if (timestampRegex.matches(timestamps[1])) {
                    formatter.parse(timestamps[1])!!.time
                } else {
                    null
                },
                forecast6hrTimestamp = if (timestampRegex.matches(timestamps[2])) {
                    formatter.parse(timestamps[2])!!.time
                } else {
                    null
                }
            )
        }
    }

    companion object {
        private const val IMD_BASE_URL = "https://mausamgram.imd.gov.in/"
        private val IMD_TIMEFRAMES = arrayOf("1hr", "3hr", "6hr")
    }
}
