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

package org.breezyweather.sources.aemet

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.aemet.json.AemetCurrentResult
import org.breezyweather.sources.aemet.json.AemetDailyResult
import org.breezyweather.sources.aemet.json.AemetHourlyResult
import org.breezyweather.sources.aemet.json.AemetNormalsResult
import org.breezyweather.sources.aemet.json.AemetStationsResult
import org.breezyweather.sources.getWindDegree
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class AemetService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : AemetServiceStub(context) {

    override val privacyPolicyUrl = "https://www.aemet.es/es/nota_legal"

    private val mApi by lazy {
        client
            .baseUrl(AEMET_BASE_URL)
            .build()
            .create(AemetApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.aemet.es/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()

        val municipio = location.parameters.getOrElse(id) { null }?.getOrElse("municipio") { null }
        val estacion = location.parameters.getOrElse(id) { null }?.getOrElse("estacion") { null }
        if (municipio.isNullOrEmpty() || estacion.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrentUrl(
                apiKey = apiKey,
                estacion = estacion
            ).map {
                val path = it.datos?.substringAfter(AEMET_BASE_URL)
                if (!path.isNullOrEmpty()) {
                    mApi.getCurrent(
                        apiKey = apiKey,
                        path = path
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.CURRENT] = it
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    emptyList()
                }
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormalsUrl(
                apiKey = apiKey,
                estacion = estacion
            ).map {
                val path = it.datos?.substringAfter(AEMET_BASE_URL)
                if (!path.isNullOrEmpty()) {
                    mApi.getNormals(
                        apiKey = apiKey,
                        path = path
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.NORMALS] = it
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    emptyList()
                }
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecastUrl(
                apiKey = apiKey,
                range = "diaria",
                municipio = municipio
            ).map { result ->
                val path = result.datos?.substringAfter(AEMET_BASE_URL)
                if (path.isNullOrEmpty()) throw InvalidOrIncompleteDataException()
                mApi.getDaily(
                    apiKey = apiKey,
                    path = path
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.FORECAST] = it
                    Observable.just(emptyList())
                }.blockingFirst()
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecastUrl(
                apiKey = apiKey,
                range = "horaria",
                municipio = municipio
            ).map { result ->
                val path = result.datos?.substringAfter(AEMET_BASE_URL)
                if (path.isNullOrEmpty()) throw InvalidOrIncompleteDataException()
                mApi.getHourly(
                    apiKey = apiKey,
                    path = path
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.FORECAST] = it
                    Observable.just(emptyList())
                }.blockingFirst()
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, daily, hourly, normals) {
                currentResult: List<AemetCurrentResult>,
                dailyResult: List<AemetDailyResult>,
                hourlyResult: List<AemetHourlyResult>,
                normalsResult: List<AemetNormalsResult>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, location, dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, location, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        currentResult: List<AemetCurrentResult>,
    ): CurrentWrapper? {
        return currentResult.lastOrNull()?.let {
            CurrentWrapper(
                temperature = TemperatureWrapper(
                    temperature = it.ta?.celsius
                ),
                wind = Wind(
                    degree = it.dv,
                    speed = it.vv?.metersPerSecond,
                    gusts = it.vmax?.metersPerSecond
                ),
                relativeHumidity = it.hr?.percent,
                dewPoint = it.tpr?.celsius,
                pressure = it.pres?.hectopascals,
                visibility = it.vis?.meters
            )
        }
    }

    private fun getNormals(
        normalsResult: List<AemetNormalsResult>,
    ): Map<Month, Normals> {
        return normalsResult
            .filter { it.mes?.toIntOrNull() != null && it.mes.toInt() in 1..12 }
            .associate {
                Month.of(it.mes!!.toInt()) to Normals(
                    daytimeTemperature = it.max?.toDoubleOrNull()?.celsius,
                    nighttimeTemperature = it.min?.toDoubleOrNull()?.celsius
                )
            }
    }

    private fun getDailyForecast(
        context: Context,
        location: Location,
        dailyResult: List<AemetDailyResult>,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        formatter.timeZone = location.timeZone
        var date: String
        var time: Long
        val wxMap = mutableMapOf<Long, String?>()
        val ppMap = mutableMapOf<Long, Double?>()
        val maxTMap = mutableMapOf<Long, Double?>()
        val minTMap = mutableMapOf<Long, Double?>()
        val maxAtMap = mutableMapOf<Long, Double?>()
        val minAtMap = mutableMapOf<Long, Double?>()
        val wdMap = mutableMapOf<Long, Double?>()
        val wsMap = mutableMapOf<Long, Double?>()
        val wgMap = mutableMapOf<Long, Double?>()
        val maxRhMap = mutableMapOf<Long, Double?>()
        val minRhMap = mutableMapOf<Long, Double?>()
        val uviMap = mutableMapOf<Long, Double?>()

        dailyResult.forEach { result ->
            result.prediccion?.dia?.forEach { day ->
                date = day.fecha.substringBefore("T")
                time = formatter.parse(date)!!.time
                day.probPrecipitacion?.forEach {
                    if (it.periodo == null || it.periodo == "00-24") {
                        ppMap[time] = it.value
                    }
                }
                day.estadoCielo?.forEach {
                    if (it.periodo == null || it.periodo == "00-24") {
                        wxMap[time] = it.value
                    }
                }
                day.viento?.forEach {
                    if (it.periodo == null || it.periodo == "00-24") {
                        wdMap[time] = getWindDegree(it.direccion)
                        wsMap[time] = it.velocidad
                    }
                }
                day.rachaMax?.forEach {
                    if (it.periodo == null || it.periodo == "00-24") {
                        wgMap[time] = it.value?.toDoubleOrNull()
                    }
                }
                maxTMap[time] = day.temperatura?.maxima
                minTMap[time] = day.temperatura?.minima
                maxAtMap[time] = day.sensTermica?.maxima
                minAtMap[time] = day.sensTermica?.minima
                maxRhMap[time] = day.humedadRelativa?.maxima
                minRhMap[time] = day.humedadRelativa?.minima
                uviMap[time] = day.uvMax
            }
        }

        wxMap.keys.sorted().forEach { key ->
            dailyList.add(
                DailyWrapper(
                    date = Date(key),
                    day = HalfDayWrapper(
                        weatherText = getWeatherText(context, wxMap.getOrElse(key) { null }),
                        weatherCode = getWeatherCode(wxMap.getOrElse(key) { null }),
                        temperature = TemperatureWrapper(
                            temperature = maxTMap.getOrElse(key) { null }?.celsius,
                            feelsLike = maxAtMap.getOrElse(key) { null }?.celsius
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = ppMap.getOrElse(key) { null }?.percent
                        ),
                        wind = Wind(
                            degree = wdMap.getOrElse(key) { null },
                            speed = wsMap.getOrElse(key) { null }?.kilometersPerHour,
                            gusts = wgMap.getOrElse(key) { null }?.kilometersPerHour
                        )
                    ),
                    night = HalfDayWrapper(
                        weatherText = getWeatherText(context, wxMap.getOrElse(key) { null }),
                        weatherCode = getWeatherCode(wxMap.getOrElse(key) { null }),
                        temperature = TemperatureWrapper(
                            temperature = minTMap.getOrElse(key) { null }?.celsius,
                            feelsLike = minAtMap.getOrElse(key) { null }?.celsius
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = ppMap.getOrElse(key) { null }?.percent
                        ),
                        wind = Wind(
                            degree = wdMap.getOrElse(key) { null },
                            speed = wsMap.getOrElse(key) { null }?.metersPerSecond,
                            gusts = wgMap.getOrElse(key) { null }?.metersPerSecond
                        )
                    ),
                    relativeHumidity = DailyRelativeHumidity(
                        max = maxRhMap.getOrElse(key) { null }?.percent,
                        min = minRhMap.getOrElse(key) { null }?.percent
                    ),
                    uV = UV(
                        index = uviMap.getOrElse(key) { null }
                    )
                )
            )
        }

        return dailyList
    }

    private fun getHourlyForecast(
        context: Context,
        location: Location,
        hourlyResult: List<AemetHourlyResult>,
    ): List<HourlyWrapper> {
        val hourlyList = mutableListOf<HourlyWrapper>()
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.ENGLISH)
        formatter.timeZone = location.timeZone
        var date: String
        var time: Long
        val wxMap = mutableMapOf<Long, String?>()
        val prMap = mutableMapOf<Long, Double?>()
        val ppMap = mutableMapOf<Long, Double?>()
        val ptMap = mutableMapOf<Long, Double?>()
        val snMap = mutableMapOf<Long, Double?>()
        val psMap = mutableMapOf<Long, Double?>()
        val tMap = mutableMapOf<Long, Double?>()
        val atMap = mutableMapOf<Long, Double?>()
        val rhMap = mutableMapOf<Long, Double?>()
        val wdMap = mutableMapOf<Long, Double?>()
        val wsMap = mutableMapOf<Long, Double?>()
        val wgMap = mutableMapOf<Long, Double?>()

        hourlyResult.forEach { result ->
            result.prediccion?.dia?.forEach { day ->
                date = day.fecha.substringBefore("T")
                day.estadoCielo?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    wxMap[time] = it.value
                }
                day.precipitacion?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    prMap[time] = it.value?.toDoubleOrNull()
                }
                day.probPrecipitacion?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    ppMap[time] = it.value?.toDoubleOrNull()
                }
                day.probTormenta?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    ptMap[time] = it.value?.toDoubleOrNull()
                }
                day.nieve?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    snMap[time] = it.value?.toDoubleOrNull()
                }
                day.probNieve?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    psMap[time] = it.value?.toDoubleOrNull()
                }
                day.temperatura?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    tMap[time] = it.value?.toDoubleOrNull()
                }
                day.sensTermica?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    atMap[time] = it.value?.toDoubleOrNull()
                }
                day.humedadRelativa?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    rhMap[time] = it.value?.toDoubleOrNull()
                }
                day.vientoAndRachaMax?.forEach {
                    time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                    it.direccion?.first()?.let { direction ->
                        wdMap[time] = getWindDegree(direction)
                    }
                    it.velocidad?.first()?.let { speed ->
                        wsMap[time] = speed.toDoubleOrNull()
                    }
                    it.value?.let { gusts ->
                        wgMap[time] = gusts.toDoubleOrNull()
                    }
                }
            }
        }

        // Precipitation probabilities are forecast once every 6 hours.
        // Fill in the gaps.
        var lastPp: Double? = null
        var lastPt: Double? = null
        var lastPs: Double? = null
        wxMap.keys.sorted().forEach { key ->
            if (ppMap.containsKey(key)) {
                lastPp = ppMap[key]
            } else {
                ppMap[key] = lastPp
            }
            if (ptMap.containsKey(key)) {
                lastPt = ptMap[key]
            } else {
                ptMap[key] = lastPt
            }
            if (psMap.containsKey(key)) {
                lastPs = psMap[key]
            } else {
                psMap[key] = lastPs
            }
        }

        wxMap.keys.sorted().forEach { key ->
            hourlyList.add(
                HourlyWrapper(
                    date = Date(key),
                    weatherText = getWeatherText(context, wxMap.getOrElse(key) { null }),
                    weatherCode = getWeatherCode(wxMap.getOrElse(key) { null }),
                    temperature = TemperatureWrapper(
                        temperature = tMap.getOrElse(key) { null }?.celsius,
                        feelsLike = atMap.getOrElse(key) { null }?.celsius
                    ),
                    precipitation = Precipitation(
                        total = prMap.getOrElse(key) { null }?.millimeters,
                        snow = snMap.getOrElse(key) { null }?.millimeters
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = ppMap.getOrElse(key) { null }?.percent,
                        thunderstorm = ptMap.getOrElse(key) { null }?.percent,
                        snow = psMap.getOrElse(key) { null }?.percent
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null }?.kilometersPerHour,
                        gusts = wgMap.getOrElse(key) { null }?.kilometersPerHour
                    ),
                    relativeHumidity = rhMap.getOrElse(key) { null }?.percent
                )
            )
        }
        return hourlyList
    }

    // Source: https://www.aemet.es/es/eltiempo/prediccion/espana/ayuda
    private fun getWeatherText(
        context: Context,
        code: String?,
    ): String? {
        return code?.let {
            with(code) {
                when {
                    startsWith("11") -> context.getString(R.string.common_weather_text_clear_sky)
                    startsWith("12") -> context.getString(R.string.common_weather_text_mostly_clear)
                    startsWith("13") -> context.getString(R.string.common_weather_text_partly_cloudy)
                    startsWith("14") -> context.getString(R.string.common_weather_text_cloudy)
                    startsWith("15") -> context.getString(R.string.common_weather_text_cloudy)
                    startsWith("16") -> context.getString(R.string.common_weather_text_overcast)
                    startsWith("17") -> context.getString(R.string.common_weather_text_mostly_clear)
                    startsWith("2") -> context.getString(R.string.common_weather_text_rain)
                    startsWith("3") -> context.getString(R.string.common_weather_text_snow)
                    startsWith("4") -> context.getString(R.string.common_weather_text_rain_light)
                    startsWith("5") -> context.getString(R.string.weather_kind_thunderstorm)
                    startsWith("6") -> context.getString(R.string.weather_kind_thunderstorm)
                    startsWith("7") -> context.getString(R.string.common_weather_text_snow_light)
                    startsWith("81") -> context.getString(R.string.common_weather_text_fog)
                    startsWith("82") -> context.getString(R.string.common_weather_text_mist)
                    startsWith("83") -> context.getString(R.string.weather_kind_haze)
                    else -> null
                }
            }
        }
    }

    private fun getWeatherCode(
        code: String?,
    ): WeatherCode? {
        return code?.let {
            with(code) {
                when {
                    startsWith("11") -> WeatherCode.CLEAR
                    startsWith("12") -> WeatherCode.CLEAR
                    startsWith("13") -> WeatherCode.PARTLY_CLOUDY
                    startsWith("14") -> WeatherCode.CLOUDY
                    startsWith("15") -> WeatherCode.CLOUDY
                    startsWith("16") -> WeatherCode.CLOUDY
                    startsWith("17") -> WeatherCode.CLEAR
                    startsWith("2") -> WeatherCode.RAIN
                    startsWith("3") -> WeatherCode.SNOW
                    startsWith("4") -> WeatherCode.RAIN
                    startsWith("5") -> WeatherCode.THUNDERSTORM
                    startsWith("6") -> WeatherCode.THUNDERSTORM
                    startsWith("7") -> WeatherCode.SNOW
                    startsWith("81") -> WeatherCode.FOG
                    startsWith("82") -> WeatherCode.FOG
                    startsWith("83") -> WeatherCode.HAZE
                    else -> null
                }
            }
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val municipio = location.parameters.getOrElse(id) { null }?.getOrElse("municipio") { null }
        val estacion = location.parameters.getOrElse(id) { null }?.getOrElse("estacion") { null }

        return municipio.isNullOrEmpty() || estacion.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val apiKey = getApiKeyOrDefault()

        val url = "https://www.aemet.es/es/eltiempo/prediccion/municipios/geolocalizacion?" +
            "munhome=no_mun&y=${location.latitude}&x=${location.longitude}"
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute().use { call ->
            if (call.isSuccessful) {
                call.body.string()
            } else {
                throw InvalidLocationException()
            }
        }
        val matchResult = Regex("""<a href='/es/eltiempo/prediccion/municipios/[^']+-id(\d+)'>""").find(response)
        val municipio = matchResult?.groups?.get(1)?.value
        if (municipio.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        val stationList = mApi.getStationsUrl(apiKey).map {
            val path = it.datos?.substringAfter(AEMET_BASE_URL)
            if (path.isNullOrEmpty()) throw InvalidLocationException()
            mApi.getStations(
                apiKey = apiKey,
                path = path
            ).blockingFirst()
        }

        return stationList.map {
            mapOf(
                "municipio" to municipio,
                "estacion" to convertLocation(location, it)
            )
        }
    }

    private fun convertLocation(
        location: Location,
        stationList: List<AemetStationsResult>,
    ): String {
        var distance: Double
        var nearestDistance = Double.POSITIVE_INFINITY
        var nearestStation = ""
        var stationLatitude: Double?
        var stationLongitude: Double?

        stationList.forEach {
            if (it.latitud != null && it.longitud != null) {
                stationLatitude = getDecimalDegrees(it.latitud)
                stationLongitude = getDecimalDegrees(it.longitud)
                if (stationLatitude != null && stationLongitude != null) {
                    distance = SphericalUtil.computeDistanceBetween(
                        LatLng(location.latitude, location.longitude),
                        LatLng(stationLatitude, stationLongitude)
                    )
                    if (distance < nearestDistance && it.indicativo != null) {
                        nearestDistance = distance
                        nearestStation = it.indicativo
                    }
                }
            }
        }
        return nearestStation
    }

    private fun getDecimalDegrees(
        dms: String,
    ): Double? {
        if (!Regex("""^\d{6}[NESW]$""").matches(dms)) return null
        return (
            dms.substring(0, 2).toDouble() +
                dms.substring(2, 4).toDouble().div(60.0) +
                dms.substring(4, 6).toDouble().div(3600.0)
            ) *
            if (dms.substring(6, 7) == "S" || dms.substring(6, 7) == "W") {
                -1.0
            } else {
                1.0
            }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.AEMET_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_aemet_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            )
        )
    }

    companion object {
        private const val AEMET_BASE_URL = "https://opendata.aemet.es/opendata/"
    }
}
