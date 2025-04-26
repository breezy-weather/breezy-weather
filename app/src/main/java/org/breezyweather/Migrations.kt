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

package org.breezyweather

import android.Manifest
import android.content.Context
import android.os.Build
import breezyweather.data.location.LocationRepository
import breezyweather.domain.source.SourceFeature
import kotlinx.coroutines.runBlocking
import org.breezyweather.background.forecast.TodayForecastNotificationJob
import org.breezyweather.background.forecast.TomorrowForecastNotificationJob
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.breezyweather.common.basic.models.options.appearance.HourlyTrendDisplay
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.main.utils.StatementManager
import java.io.File

object Migrations {

    /**
     * Performs a migration when the application is updated.
     *
     * @return true if a migration is performed, false otherwise.
     */
    fun upgrade(context: Context, sourceManager: SourceManager, locationRepository: LocationRepository): Boolean {
        val lastVersionCode = SettingsManager.getInstance(context).lastVersionCode
        val oldVersion = lastVersionCode
        if (oldVersion < BuildConfig.VERSION_CODE) {
            if (oldVersion > 0) { // Not fresh install
                if (oldVersion < 50000) {
                    // V5.0.0 adds many new charts
                    // Adding it to people who customized their hourly trends tabs so they don't miss
                    // this new feature. This can still be removed by user from settings
                    // as this code is only executed once, after migrating from a version < 5.0.0
                    try {
                        val curHourlyTrendDisplayList = HourlyTrendDisplay.toValue(
                            SettingsManager.getInstance(context).hourlyTrendDisplayList
                        )
                        if (curHourlyTrendDisplayList != SettingsManager.DEFAULT_HOURLY_TREND_DISPLAY) {
                            SettingsManager.getInstance(context).hourlyTrendDisplayList =
                                HourlyTrendDisplay.toHourlyTrendDisplayList(
                                    "$curHourlyTrendDisplayList&feels_like&humidity&pressure&cloud_cover&visibility"
                                )
                        }
                        val curDailyTrendDisplayList = DailyTrendDisplay.toValue(
                            SettingsManager.getInstance(context).dailyTrendDisplayList
                        )
                        if (curDailyTrendDisplayList != SettingsManager.DEFAULT_DAILY_TREND_DISPLAY) {
                            SettingsManager.getInstance(context).dailyTrendDisplayList =
                                DailyTrendDisplay.toDailyTrendDisplayList("$curDailyTrendDisplayList&feels_like")
                        }
                    } catch (ignored: Throwable) {
                        // ignored
                    }

                    // Delete old ObjectBox database
                    context.applicationInfo?.dataDir?.let {
                        val file = File("$it/files/objectbox/")
                        if (file.exists() && file.isDirectory) {
                            file.deleteRecursively()
                        }
                    }
                }

                if (oldVersion < 50102) {
                    // V5.1.2 adds daily sunshine chart
                    try {
                        val curDailyTrendDisplayList =
                            DailyTrendDisplay.toValue(SettingsManager.getInstance(context).dailyTrendDisplayList)
                        if (curDailyTrendDisplayList != SettingsManager.DEFAULT_DAILY_TREND_DISPLAY) {
                            SettingsManager.getInstance(context).dailyTrendDisplayList =
                                DailyTrendDisplay.toDailyTrendDisplayList("$curDailyTrendDisplayList&sunshine")
                        }
                    } catch (ignored: Throwable) {
                        // ignored
                    }
                }

                if (oldVersion < 50108) {
                    // V5.1.8 adds precipitation nowcast as a dedicated card
                    try {
                        val curCardDisplayList =
                            CardDisplay.toValue(SettingsManager.getInstance(context).cardDisplayList)
                        if (curCardDisplayList != SettingsManager.DEFAULT_CARD_DISPLAY) {
                            SettingsManager.getInstance(context).cardDisplayList =
                                CardDisplay.toCardDisplayList("precipitation_nowcast&$curCardDisplayList")
                        }
                    } catch (ignored: Throwable) {
                        // ignored
                    }
                }

                if (oldVersion < 50400) {
                    // V5.4.0 changes the way empty source value work on locations
                    runBlocking {
                        locationRepository.getAllLocations(withParameters = false)
                            .forEach {
                                val source = sourceManager.getWeatherSource(it.forecastSource)
                                if (source != null) {
                                    locationRepository.update(
                                        it.copy(
                                            currentSource = if (it.currentSource.isNullOrEmpty() &&
                                                SourceFeature.CURRENT in source.supportedFeatures &&
                                                source.isFeatureSupportedForLocation(it, SourceFeature.CURRENT)
                                            ) {
                                                source.id
                                            } else {
                                                it.currentSource
                                            },
                                            airQualitySource = if (it.airQualitySource.isNullOrEmpty() &&
                                                SourceFeature.AIR_QUALITY in source.supportedFeatures &&
                                                source.isFeatureSupportedForLocation(it, SourceFeature.AIR_QUALITY)
                                            ) {
                                                source.id
                                            } else {
                                                it.airQualitySource
                                            },
                                            pollenSource = if (it.pollenSource.isNullOrEmpty() &&
                                                SourceFeature.POLLEN in source.supportedFeatures &&
                                                source.isFeatureSupportedForLocation(it, SourceFeature.POLLEN)
                                            ) {
                                                source.id
                                            } else {
                                                it.pollenSource
                                            },
                                            minutelySource = if (it.minutelySource.isNullOrEmpty() &&
                                                SourceFeature.MINUTELY in source.supportedFeatures &&
                                                source.isFeatureSupportedForLocation(it, SourceFeature.MINUTELY)
                                            ) {
                                                source.id
                                            } else {
                                                it.minutelySource
                                            },
                                            alertSource = if (it.alertSource.isNullOrEmpty() &&
                                                SourceFeature.ALERT in source.supportedFeatures &&
                                                source.isFeatureSupportedForLocation(it, SourceFeature.ALERT)
                                            ) {
                                                source.id
                                            } else {
                                                it.alertSource
                                            },
                                            normalsSource = if (it.normalsSource.isNullOrEmpty() &&
                                                SourceFeature.NORMALS in source.supportedFeatures &&
                                                source.isFeatureSupportedForLocation(it, SourceFeature.NORMALS)
                                            ) {
                                                source.id
                                            } else {
                                                it.normalsSource
                                            }
                                        )
                                    )
                                }
                            }
                    }
                }

                if (oldVersion < 50402) {
                    try {
                        // We cannot determine if the permission was permanently denied in the past. That is why we
                        // need to update the state for all users updating from an older version.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            StatementManager(context).setPermissionDenied(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } catch (ignored: Throwable) {
                        // ignored
                    }
                }

                if (oldVersion < 50403) {
                    // V5.4.3 no longer uses forecastSource as reverseGeocodingSource. Migrates current location
                    runBlocking {
                        locationRepository.getAllLocations(withParameters = false)
                            .forEach {
                                if (it.isCurrentPosition) {
                                    val source = sourceManager.getReverseGeocodingSource(it.forecastSource)
                                    if (source != null && source.isReverseGeocodingSupportedForLocation(it)) {
                                        locationRepository.update(
                                            it.copy(
                                                reverseGeocodingSource = source.id
                                            )
                                        )
                                    }
                                }
                            }
                    }
                }

                if (oldVersion < 50407) {
                    runBlocking {
                        // V5.4.7 removes incorrect INSEE code for Paris, Marseille, Lyon with ATMO France
                        locationRepository.updateParameters(
                            source = "atmofrance",
                            parameter = "citycode",
                            values = mapOf(
                                "75101" to "75056", // Paris
                                "75102" to "75056", // Paris
                                "75103" to "75056", // Paris
                                "75104" to "75056", // Paris
                                "75105" to "75056", // Paris
                                "75106" to "75056", // Paris
                                "75107" to "75056", // Paris
                                "75108" to "75056", // Paris
                                "75109" to "75056", // Paris
                                "75110" to "75056", // Paris
                                "75111" to "75056", // Paris
                                "75112" to "75056", // Paris
                                "75113" to "75056", // Paris
                                "75114" to "75056", // Paris
                                "75115" to "75056", // Paris
                                "75116" to "75056", // Paris
                                "75117" to "75056", // Paris
                                "75118" to "75056", // Paris
                                "75119" to "75056", // Paris
                                "75120" to "75056", // Paris
                                "13201" to "13055", // Marseille
                                "13202" to "13055", // Marseille
                                "13203" to "13055", // Marseille
                                "13204" to "13055", // Marseille
                                "13205" to "13055", // Marseille
                                "13206" to "13055", // Marseille
                                "13207" to "13055", // Marseille
                                "13208" to "13055", // Marseille
                                "13209" to "13055", // Marseille
                                "13210" to "13055", // Marseille
                                "13211" to "13055", // Marseille
                                "13212" to "13055", // Marseille
                                "13213" to "13055", // Marseille
                                "13214" to "13055", // Marseille
                                "13215" to "13055", // Marseille
                                "13216" to "13055", // Marseille
                                "69381" to "69123", // Lyon
                                "69382" to "69123", // Lyon
                                "69383" to "69123", // Lyon
                                "69384" to "69123", // Lyon
                                "69385" to "69123", // Lyon
                                "69386" to "69123", // Lyon
                                "69387" to "69123", // Lyon
                                "69388" to "69123", // Lyon
                                "69389" to "69123" // Lyon
                            )
                        )

                        // V5.4.7 migrates some Open-Meteo weather models
                        locationRepository.updateParameters(
                            source = "openmeteo",
                            parameter = "weatherModels",
                            values = mapOf(
                                "ecmwf_ifs04" to "ecmwf_ifs025",
                                "ecmwf_aifs025" to "ecmwf_aifs025_single",
                                "arpae_cosmo_seamless" to "italia_meteo_arpae_icon_2i",
                                "arpae_cosmo_2i" to "italia_meteo_arpae_icon_2i",
                                "arpae_cosmo_5m" to "italia_meteo_arpae_icon_2i"
                            )
                        )
                    }
                }
            }

            SettingsManager.getInstance(context).lastVersionCode = BuildConfig.VERSION_CODE

            // Always set up background tasks to ensure they're running
            WeatherUpdateJob.setupTask(context) // This will also refresh data immediately
            TodayForecastNotificationJob.setupTask(context, false)
            TomorrowForecastNotificationJob.setupTask(context, false)

            return oldVersion != 0
        }

        return false
    }
}
