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

package org.breezyweather.sources.librewxr

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.radar.RadarWrapper
import breezyweather.domain.radar.model.RadarColorScheme
import breezyweather.domain.radar.model.RadarInfo
import breezyweather.domain.radar.model.RadarUrl
import breezyweather.domain.source.SourceContinent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.breezyweather.R
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.RadarTileSource
import org.breezyweather.domain.settings.SourceConfigStore
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

/**
 * TODO: Looks like we could use it as an Alert source
 * TODO: Missing attributions
 */
class LibreWxrService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder,
) : HttpSource(), RadarTileSource, ConfigurableSource {
    override val id = "librewxr"
    override val name = "LibreWXR"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://librewxr.net/terms"

    private val mApi: LibreWxrApi
        get() {
            return client
                .baseUrl(instance!!)
                .build()
                .create(LibreWxrApi::class.java)
        }

    override suspend fun getRadarInfo(context: Context): RadarWrapper? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val weatherMaps = mApi.getWeatherMaps()

                if (!weatherMaps.host.isNullOrEmpty()) {
                    RadarWrapper(
                        info = buildMap {
                            weatherMaps.radar?.let { radar ->
                                put(
                                    "radar",
                                    RadarInfo(
                                        name = context.getString(R.string.map_radar),
                                        colorSchemes = radar.colorSchemes
                                            ?.filter { it.name != null && it.id != null }
                                            ?.map { colorScheme ->
                                                RadarColorScheme(
                                                    name = colorScheme.name!!,
                                                    replacePath = colorScheme.id.toString()
                                                )
                                            },
                                        url = buildList {
                                            radar.past?.forEach {
                                                if (it.time != null && it.path != null) {
                                                    add(
                                                        RadarUrl(
                                                            Date(it.time.times(1000L)),
                                                            "${weatherMaps.host}${it.path}/256/{z}/{x}/{y}/\$color/1_0.png",
                                                            context.getString(R.string.map_past)
                                                        )
                                                    )
                                                }
                                            }
                                            radar.nowcast?.forEach {
                                                if (it.time != null && it.path != null) {
                                                    add(
                                                        RadarUrl(
                                                            Date(it.time.times(1000L)),
                                                            "${weatherMaps.host}${it.path}/256/{z}/{x}/{y}/\$color/1_0.png",
                                                            context.getString(R.string.map_nowcast)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )
                                )
                            }
                            weatherMaps.satellite?.let { satellite ->
                                put(
                                    "satellite",
                                    RadarInfo(
                                        name = context.getString(R.string.map_satellite),
                                        url = buildList {
                                            satellite.infrared?.forEach {
                                                if (it.time != null && it.path != null) {
                                                    add(
                                                        RadarUrl(
                                                            Date(it.time.times(1000L)),
                                                            "${weatherMaps.host}${it.path}/256/{z}/{x}/{y}/0/0_0.png",
                                                            context.getString(R.string.map_past)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )
                                )
                            }
                        }
                    )
                } else {
                    null
                }
            }.getOrElse {
                // TODO
                null
            }
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", it).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", null) ?: LIBREWXR_BASE_URL

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_librewxr_instance,
                summary = { _, content ->
                    content.ifEmpty {
                        LIBREWXR_BASE_URL
                    }
                },
                content = if (instance != LIBREWXR_BASE_URL) instance else null,
                placeholder = LIBREWXR_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == LIBREWXR_BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    companion object {
        private const val LIBREWXR_BASE_URL = "https://api.librewxr.net/"
    }
}
