package org.breezyweather.sources.fmi

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.CLOUD_COVER_BKN
import org.breezyweather.common.extensions.CLOUD_COVER_FEW
import org.breezyweather.common.extensions.CLOUD_COVER_OVC
import org.breezyweather.common.extensions.CLOUD_COVER_SCT
import org.breezyweather.common.extensions.CLOUD_COVER_SKC
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.sources.common.xml.CapAlert
import org.breezyweather.sources.fmi.xml.FmiSimpleResult
import org.breezyweather.sources.fmi.xml.FmiStationsResult
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class FmiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") client: Retrofit.Builder,
) : FmiServiceStub(context) {

    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fi") -> "https://www.ilmatieteenlaitos.fi/tietosuoja"
                startsWith("sv") -> "https://sv.ilmatieteenlaitos.fi/dataskydd"
                else -> "https://en.ilmatieteenlaitos.fi/data-protection"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(FMI_BASE_URL)
            .build()
            .create(FmiApi::class.java)
    }

    private val mAlertsApi by lazy {
        client
            .baseUrl(FMI_ALERTS_BASE_URL)
            .build()
            .create(FmiAlertsApi::class.java)
    }

    override val attributionLinks = mapOf(
        "Ilmatieteen Laitos" to "https://www.ilmatieteenlaitos.fi/",
        "Meteorologiska Institutet" to "https://sv.ilmatieteenlaitos.fi/",
        "Finnish Meteorological Institute" to "https://en.ilmatieteenlaitos.fi/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        if (arrayOf(SourceFeature.CURRENT, SourceFeature.NORMALS).any { it in requestedFeatures } &&
            currentStation.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        val hourFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH':00:00Z'", Locale.ENGLISH)
        hourFormatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val minuteFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00Z'", Locale.ENGLISH)
        minuteFormatter.timeZone = TimeZone.getTimeZone("Etc/UTC")

        val latlon = "${location.latitude},${location.longitude}"

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            // limit to the last hour, otherwise the call will download 720 minutes (12 hours)
            val starttime = System.currentTimeMillis().minus(1.hours.inWholeMilliseconds)
            val result = mApi.getCurrent(
                fmisid = currentStation!!,
                starttime = minuteFormatter.format(Date(starttime))
            ).execute().body()
            if (result == null) {
                failedFeatures[SourceFeature.CURRENT] = WeatherException()
                FmiSimpleResult()
            } else {
                result
            }
        } else {
            FmiSimpleResult()
        }

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            // FMI has 10-day forecast, but data points on the 10th day are non-uniformly sparse
            // leading to random crashes of the app. Safer to keep to 9 days.
            val endtime = System.currentTimeMillis().plus(9.days.inWholeMilliseconds)
            val result = mApi.getForecast(
                latlon = latlon,
                storedQueryId = "fmi::forecast::edited::weather::scandinavia::point::simple",
                endtime = hourFormatter.format(Date(endtime))
            ).execute().body()
            if (result == null) {
                failedFeatures[SourceFeature.FORECAST] = WeatherException()
                FmiSimpleResult()
            } else {
                result
            }
        } else {
            FmiSimpleResult()
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            // This endpoint gets the pollutant concentration forecasts only.
            // There are also two endpoints for current/recent pollutant observations,
            // but more work is needed figure out which station IDs report to either endpoint.
            val endtime = System.currentTimeMillis().plus(4.days.inWholeMilliseconds)
            val result = mApi.getForecast(
                latlon = latlon,
                storedQueryId = "fmi::forecast::silam::airquality::surface::point::simple",
                endtime = hourFormatter.format(Date(endtime))
            ).execute().body()
            if (result == null) {
                failedFeatures[SourceFeature.AIR_QUALITY] = WeatherException()
                FmiSimpleResult()
            } else {
                result
            }
        } else {
            FmiSimpleResult()
        }

        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            val result = mApi.getNormals(
                fmisid = currentStation!!
            ).execute().body()
            if (result == null) {
                failedFeatures[SourceFeature.NORMALS] = WeatherException()
                FmiSimpleResult()
            } else {
                result
            }
        } else {
            FmiSimpleResult()
        }

        var someAlertsFailed = false
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            val alertsResult = mAlertsApi.getAlerts().execute().body()
            if (alertsResult != null) {
                alertsResult.let { alertFeed ->
                    alertFeed.channel?.items?.filter {
                        !(it.title?.value?.startsWith("REMOVED:", ignoreCase = true) ?: false) &&
                            !it.link?.value.isNullOrEmpty()
                    }?.map {
                        it.link?.value?.let { link ->
                            mAlertsApi.getAlert(link).onErrorResumeNext {
                                someAlertsFailed = true
                                Observable.just(CapAlert())
                            }
                        } ?: Observable.just(CapAlert())
                    }
                } ?: listOf<Observable<CapAlert>>()
            } else {
                someAlertsFailed = true
                listOf<Observable<CapAlert>>()
            }
        } else {
            listOf<Observable<CapAlert>>()
        }.map {
            it.blockingFirst()
        }
        if (someAlertsFailed) {
            failedFeatures[SourceFeature.ALERT] = InvalidOrIncompleteDataException()
        }

        return Observable.just(
            WeatherWrapper(
                current = getCurrent(context, current),
                dailyForecast = getDailyForecast(location, forecast),
                hourlyForecast = getHourlyForecast(context, forecast),
                airQuality = getAirQuality(airQuality),
                alertList = getAlertList(context, location, alerts),
                normals = getNormals(normals),
                failedFeatures = failedFeatures
            )
        )
    }

    private fun getCurrent(
        context: Context,
        currentResult: FmiSimpleResult,
    ): CurrentWrapper? {
        return currentResult.members?.let {
            val cloudCover = extractLatest(it, "n_man")?.times(12.5)
            CurrentWrapper(
                weatherText = getCurrentWeatherText(context, (extractLatest(it, "wawa") ?: 0).toInt(), cloudCover),
                weatherCode = getCurrentWeatherCode((extractLatest(it, "wawa") ?: 0).toInt(), cloudCover),
                temperature = TemperatureWrapper(
                    temperature = extractLatest(it, "t2m")?.celsius
                ),
                wind = Wind(
                    degree = extractLatest(it, "wd_10min"),
                    speed = extractLatest(it, "ws_10min")?.metersPerSecond,
                    gusts = extractLatest(it, "wg_10min")?.metersPerSecond
                ),
                relativeHumidity = extractLatest(it, "rh")?.percent,
                dewPoint = extractLatest(it, "td")?.celsius,
                pressure = extractLatest(it, "p_sea")?.hectopascals,
                cloudCover = cloudCover?.percent,
                visibility = extractLatest(it, "vis")?.meters
            )
        }
    }

    private fun getDailyForecast(
        location: Location,
        forecastResult: FmiSimpleResult,
    ): List<DailyWrapper>? {
        val dates = forecastResult.members?.filter {
            it.bsWfsElement?.parameterValue?.value != "NaN"
        }?.groupBy {
            it.bsWfsElement?.time?.value?.getIsoFormattedDate(location)
        }?.keys?.sortedBy { it }
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        formatter.timeZone = location.timeZone
        return dates?.map {
            DailyWrapper(
                date = formatter.parse(it!!)!!
            )
        }
    }

    private fun getHourlyForecast(
        context: Context,
        forecastResult: FmiSimpleResult,
    ): List<HourlyWrapper>? {
        val hours = forecastResult.members?.groupBy { member ->
            member.bsWfsElement?.time?.value
        }?.keys?.sortedBy { it }

        return hours?.map { hour ->
            val members = forecastResult.members.filter {
                it.bsWfsElement?.time?.value == hour
            }
            HourlyWrapper(
                date = hour!!,
                weatherText = getForecastWeatherText(context, (extract(members, "WeatherSymbol3") ?: 0).toInt()),
                weatherCode = getForecastWeatherCode((extract(members, "WeatherSymbol3") ?: 0).toInt()),
                temperature = TemperatureWrapper(
                    temperature = extract(members, "Temperature")?.celsius
                ),
                precipitation = Precipitation(
                    total = extract(members, "Precipitation1h")?.millimeters
                ),
                precipitationProbability = PrecipitationProbability(
                    total = extract(members, "PoP")?.percent,
                    thunderstorm = extract(members, "ProbabilityThunderstorm")?.percent
                ),
                wind = Wind(
                    degree = extract(members, "WindDirection"),
                    speed = extract(members, "WindSpeedMS")?.metersPerSecond,
                    gusts = extract(members, "HourlyMaximumGust")?.metersPerSecond
                ),
                relativeHumidity = extract(members, "Humidity")?.percent,
                dewPoint = extract(members, "DewPoint")?.celsius,
                pressure = extract(members, "Pressure")?.hectopascals,
                cloudCover = extract(members, "TotalCloudCover")?.percent
            )
        }
    }

    private fun getAirQuality(
        airQualityResult: FmiSimpleResult,
    ): AirQualityWrapper {
        val hours = airQualityResult.members?.filter { member ->
            member.bsWfsElement?.time?.value != null
        }?.groupBy { member ->
            member.bsWfsElement?.time?.value
        }?.keys?.sortedBy { it }?.mapNotNull { it }

        return AirQualityWrapper(
            hourlyForecast = hours?.associateWith { hour ->
                val members = airQualityResult.members.filter {
                    it.bsWfsElement?.time?.value == hour
                }
                AirQuality(
                    pM25 = extract(members, "PM25Concentration")?.microgramsPerCubicMeter,
                    pM10 = extract(members, "PM10Concentration")?.microgramsPerCubicMeter,
                    sO2 = extract(members, "SO2Concentration")?.microgramsPerCubicMeter,
                    nO2 = extract(members, "NO2Concentration")?.microgramsPerCubicMeter,
                    o3 = extract(members, "O3Concentration")?.microgramsPerCubicMeter,
                    cO = extract(members, "COConcentration")?.microgramsPerCubicMeter
                )
            }
        )
    }

    private fun getNormals(
        normalsResult: FmiSimpleResult,
    ): Map<Month, Normals> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
        return Month.entries.associateWith { month ->
            val datePattern = Regex("""^\d{4}-${"%02d".format(month.value)}-01T00:00:00Z$""")
            val monthlyRecords = normalsResult.members?.filter {
                datePattern.matches(formatter.format(it.bsWfsElement?.time?.value!!))
            }
            if (!monthlyRecords.isNullOrEmpty()) {
                Normals(
                    daytimeTemperature = extract(monthlyRecords, "TAMAXP1M")?.celsius,
                    nighttimeTemperature = extract(monthlyRecords, "TAMINP1M")?.celsius
                )
            } else {
                Normals()
            }
        }
    }

    private fun getAlertList(
        context: Context,
        location: Location,
        alerts: List<CapAlert>,
    ): List<Alert> {
        val regex = Regex("""^(Landskapet )?(.*)( Region)?$""")
        return alerts.mapNotNull { capAlert ->
            if (!capAlert.msgType?.value.equals("Cancel", ignoreCase = true)) {
                capAlert.getInfoForContext(context)?.let { info ->
                    // Try matching by region name/code first. It's much faster than calling containsPoint().
                    // We should clean up region name, as Open-Meteo adds "Landskapet" or "Region" to some regions.
                    // Then compare the region name against areas listed on the alert.
                    // This check assumes the user did not change language between adding the location and
                    // refreshing the location data. Otherwise, the region names won't match and it will
                    // fall back to calling containsPoint().
                    // If the location is added from Nominatim, there will likely be ISO 3166-2 code as well,
                    // which can be used for matching by area code.
                    val regionName = regex.replace(location.admin1 ?: "", "$2").trim()
                    val regionCode = location.admin1Code
                    val matchingRegion = (
                        info.areas?.any { area -> area.areaDesc?.value.equals(regionName, ignoreCase = true) } ?: false
                        ) || (!regionCode.isNullOrEmpty() && info.containsGeocode("ISO 3166-2", regionCode)) || (
                        location.countryCode.equals("AX", ignoreCase = true) && info.areas?.any { area ->
                            arrayOf("Åland", "Ahvenanmaa").any { area.areaDesc?.value.equals(it, ignoreCase = true) }
                        } ?: false
                        )

                    if (info.category?.value.equals("Met", ignoreCase = true) &&
                        !info.urgency?.value.equals("Past", ignoreCase = true) &&
                        (matchingRegion || info.containsPoint(LatLng(location.latitude, location.longitude)))
                    ) {
                        val severity = when (info.severity?.value) {
                            "Extreme" -> AlertSeverity.EXTREME
                            "Severe" -> AlertSeverity.SEVERE
                            "Moderate" -> AlertSeverity.MODERATE
                            "Minor" -> AlertSeverity.MINOR
                            else -> AlertSeverity.UNKNOWN
                        }
                        val title = info.event?.value ?: info.headline?.value
                        val start = info.onset?.value ?: info.effective?.value ?: capAlert.sent?.value
                        Alert(
                            alertId = capAlert.identifier?.value ?: Objects.hash(title, severity, start).toString(),
                            startDate = start,
                            endDate = info.expires?.value,
                            headline = title,
                            description = info.formatAlertText(text = info.description?.value),
                            instruction = info.formatAlertText(text = info.instruction?.value),
                            source = info.senderName?.value,
                            severity = severity,
                            color = Alert.colorFromSeverity(severity)
                        )
                    } else {
                        null
                    }
                }
            } else {
                null
            }
        }
    }

    // Source for "WaWa" codes (Sääsymbolien selitykset säähavainnoissa):
    // https://www.ilmatieteenlaitos.fi/latauspalvelun-pikaohje
    private fun getCurrentWeatherText(context: Context, symbol: Int, cloudCover: Double?): String? {
        return when (symbol) {
            0 -> cloudCover?.let {
                when {
                    it < 0.0 -> null
                    it < CLOUD_COVER_SKC -> context.getString(R.string.common_weather_text_clear_sky)
                    it < CLOUD_COVER_FEW -> context.getString(R.string.common_weather_text_mostly_clear)
                    it < CLOUD_COVER_SCT -> context.getString(R.string.common_weather_text_partly_cloudy)
                    it < CLOUD_COVER_BKN -> context.getString(R.string.common_weather_text_mostly_cloudy)
                    it < CLOUD_COVER_OVC -> context.getString(R.string.common_weather_text_cloudy)
                    it == CLOUD_COVER_OVC -> context.getString(R.string.common_weather_text_overcast)
                    else -> null
                }
            }
            4, 5 -> context.getString(R.string.weather_kind_haze)
            10 -> context.getString(R.string.common_weather_text_mist)
            20, 30, 31, 32, 33, 34 -> context.getString(R.string.common_weather_text_fog)
            21, 23, 40, 41, 60 -> context.getString(R.string.common_weather_text_rain)
            22, 50 -> context.getString(R.string.common_weather_text_drizzle)
            24, 70 -> context.getString(R.string.common_weather_text_snow)
            25 -> context.getString(R.string.common_weather_text_rain_freezing)
            61 -> context.getString(R.string.common_weather_text_rain_light)
            42, 63 -> context.getString(R.string.common_weather_text_rain_heavy)
            51 -> context.getString(R.string.common_weather_text_drizzle_light)
            52 -> context.getString(R.string.common_weather_text_drizzle_moderate)
            53 -> context.getString(R.string.common_weather_text_drizzle_heavy)
            54 -> context.getString(R.string.common_weather_text_drizzle_freezing_light)
            55 -> context.getString(R.string.common_weather_text_drizzle_freezing)
            56 -> context.getString(R.string.common_weather_text_drizzle_freezing_heavy)
            62 -> context.getString(R.string.common_weather_text_rain_moderate)
            64 -> context.getString(R.string.common_weather_text_rain_freezing_light)
            65 -> context.getString(R.string.common_weather_text_rain_freezing)
            66 -> context.getString(R.string.common_weather_text_rain_freezing_heavy)
            67 -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
            68 -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            71 -> context.getString(R.string.common_weather_text_snow_light)
            72 -> context.getString(R.string.common_weather_text_snow_moderate)
            73 -> context.getString(R.string.common_weather_text_snow_heavy)
            74 -> context.getString(R.string.fmi_weather_text_ice_pellets_light)
            75 -> context.getString(R.string.fmi_weather_text_ice_pellets_moderate)
            76 -> context.getString(R.string.fmi_weather_text_ice_pellets_heavy)
            77 -> context.getString(R.string.common_weather_text_snow_grains)
            78 -> context.getString(R.string.fmi_weather_text_ice_crystals)
            80 -> context.getString(R.string.common_weather_text_rain_showers)
            81 -> context.getString(R.string.common_weather_text_rain_showers_light)
            82 -> context.getString(R.string.common_weather_text_rain_showers_moderate)
            83, 84 -> context.getString(R.string.common_weather_text_rain_showers_heavy)
            85 -> context.getString(R.string.common_weather_text_snow_showers_light)
            86 -> context.getString(R.string.common_weather_text_snow_showers)
            87 -> context.getString(R.string.common_weather_text_snow_showers_heavy)
            89 -> context.getString(R.string.weather_kind_hail)
            else -> null
        }
    }

    private fun getCurrentWeatherCode(symbol: Int, cloudCover: Double?): WeatherCode? {
        return when (symbol) {
            0 -> cloudCover?.let {
                when {
                    it < 0.0 -> null
                    it < CLOUD_COVER_FEW -> WeatherCode.CLEAR
                    it < CLOUD_COVER_SCT -> WeatherCode.PARTLY_CLOUDY
                    it <= CLOUD_COVER_BKN -> WeatherCode.CLOUDY
                    else -> null
                }
            }
            4, 5 -> WeatherCode.HAZE
            10, 20, 30, 31, 32, 33, 34 -> WeatherCode.FOG
            21, 22, 23, 40, 41, 42, 50, 51, 52, 53, 60, 61, 62, 63, 80, 81, 82, 83, 84 -> WeatherCode.RAIN
            24, 70, 71, 72, 73, 74, 75, 76, 77, 85, 86, 87 -> WeatherCode.SNOW
            25, 54, 55, 56, 64, 65, 66, 67, 68 -> WeatherCode.SLEET
            78 -> WeatherCode.FOG // ice crystals
            89 -> WeatherCode.HAIL
            else -> null
        }
    }

    // Source for WeatherSymbol3 definitions:
    // https://www.ilmatieteenlaitos.fi/latauspalvelun-pikaohje
    private fun getForecastWeatherText(context: Context, symbol: Int): String? {
        return when (symbol) {
            1 -> context.getString(R.string.common_weather_text_clear_sky)
            2 -> context.getString(R.string.common_weather_text_partly_cloudy)
            3 -> context.getString(R.string.common_weather_text_cloudy)
            21 -> context.getString(R.string.common_weather_text_rain_showers_light)
            22 -> context.getString(R.string.common_weather_text_rain_showers)
            23 -> context.getString(R.string.common_weather_text_rain_showers_heavy)
            31 -> context.getString(R.string.common_weather_text_rain_light)
            32 -> context.getString(R.string.common_weather_text_rain)
            33 -> context.getString(R.string.common_weather_text_rain_heavy)
            41 -> context.getString(R.string.common_weather_text_snow_showers_light)
            42 -> context.getString(R.string.common_weather_text_snow_showers)
            43 -> context.getString(R.string.common_weather_text_snow_showers_heavy)
            51 -> context.getString(R.string.common_weather_text_snow_light)
            52 -> context.getString(R.string.common_weather_text_snow)
            53 -> context.getString(R.string.common_weather_text_snow_heavy)
            // originally: Ukkoskuuroja (Thundershower)
            61 -> context.getString(R.string.weather_kind_thunderstorm)
            // originally: Voimakkaita ukkoskuuroja (Heavy thundershower)
            62 -> context.getString(R.string.weather_kind_thunderstorm)
            // originally: Ukkosta (Thunder)
            63 -> context.getString(R.string.weather_kind_thunder)
            // originally: Voimakasta ukkosta (Heavy thunder -- what does that even mean?)
            64 -> context.getString(R.string.weather_kind_thunder)
            71 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers_light)
            72 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
            73 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers_heavy)
            81 -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
            82 -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            83 -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy)
            91 -> context.getString(R.string.common_weather_text_mist)
            92 -> context.getString(R.string.common_weather_text_fog)
            else -> null
        }
    }

    private fun getForecastWeatherCode(symbol: Int): WeatherCode? {
        return when (symbol) {
            1 -> WeatherCode.CLEAR
            2 -> WeatherCode.PARTLY_CLOUDY
            3 -> WeatherCode.CLOUDY
            21, 22, 23, 31, 32, 33 -> WeatherCode.RAIN
            41, 42, 43, 51, 52, 53 -> WeatherCode.SNOW
            61, 62 -> WeatherCode.THUNDERSTORM
            63, 64 -> WeatherCode.THUNDER
            71, 72, 73, 81, 82, 83 -> WeatherCode.SLEET
            91, 92 -> WeatherCode.FOG
            else -> null
        }
    }

    private fun extract(
        members: List<FmiSimpleResult.Member>,
        parameter: String,
    ): Double? {
        val value = members.firstOrNull {
            it.bsWfsElement?.parameterName?.value == parameter
        }?.bsWfsElement?.parameterValue?.value
        if (value.isNullOrEmpty() || value.equals("NaN", ignoreCase = true)) {
            return null
        } else {
            return value.toDoubleOrNull()
        }
    }

    // used only by getCurrent()
    // which will only have access to observation data points in the last hour
    // so should not check for data points that are no longer current
    private fun extractLatest(
        members: List<FmiSimpleResult.Member>,
        parameter: String,
    ): Double? {
        val timestamp = members.filter {
            it.bsWfsElement?.parameterName?.value == parameter
        }.map {
            it.bsWfsElement?.time?.value
        }.sortedBy { it }.last()

        val value = members.first {
            it.bsWfsElement?.time?.value == timestamp &&
                it.bsWfsElement?.parameterName?.value == parameter
        }.bsWfsElement?.parameterValue?.value
        if (value.isNullOrEmpty() || value.equals("NaN", ignoreCase = true)) {
            return null
        } else {
            return value.toDoubleOrNull()
        }
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }

        return (SourceFeature.CURRENT in features && currentStation.isNullOrEmpty())
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val locationParameters = mutableMapOf<String, String>()
        val posPattern = Regex("""^-?\d+(\.\d+)? -?\d+(\.\d+)?$""")
        return mApi.getStations().map { stations ->
            val currentStations = stations.members?.filter { member ->
                member.environmentalMonitoringFacility.let {
                    !(it?.identifier?.value.isNullOrEmpty()) &&
                        posPattern.matches(it.representativePoint?.point?.pos?.value ?: "") &&
                        it.belongsTo?.any { belongsTo ->
                            belongsTo.title == "Automaattinen sääasema"
                        } ?: false
                }
            } ?: emptyList()

            locationParameters["currentStation"] = convertLocationParameters(location, currentStations) ?: ""
            locationParameters
        }
    }

    // location parameters
    private fun convertLocationParameters(
        location: Location,
        members: List<FmiStationsResult.Member>,
    ): String? {
        val stationList = members.associate { member ->
            member.environmentalMonitoringFacility.let {
                val coords = it!!.representativePoint!!.point!!.pos!!.value!!.split(" ")
                it.identifier!!.value!! to LatLng(
                    coords[0].toDouble(),
                    coords[1].toDouble()
                )
            }
        }
        return LatLng(location.latitude, location.longitude).getNearestLocation(stationList, 50000.0)
    }

    companion object {
        private const val FMI_BASE_URL = "https://opendata.fmi.fi/"
        private const val FMI_ALERTS_BASE_URL = "https://alerts.fmi.fi/"
    }
}
