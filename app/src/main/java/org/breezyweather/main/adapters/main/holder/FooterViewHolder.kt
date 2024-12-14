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

package org.breezyweather.main.adapters.main.holder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.CallSuper
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import io.github.giangpham96.expandable_text_compose.ExpandableText
import org.breezyweather.R
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.main.MainActivity
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.resource.providers.ResourceProvider

class FooterViewHolder(
    private val composeView: ComposeView,
) : AbstractMainViewHolder(composeView) {

    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun onBindView(
        context: Context,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val cardMarginsVertical = ThemeManager.getInstance(context)
            .weatherThemeDelegate
            .getHomeCardMargins(context).toFloat()

        val distinctSources = mutableMapOf<String, WeatherSource?>()
        listOf(
            location.forecastSource,
            location.currentSource,
            location.airQualitySource,
            location.pollenSource,
            location.minutelySource,
            location.alertSource,
            location.normalsSource
        ).filter { !it.isNullOrEmpty() }.distinct().forEach {
            distinctSources[it!!] = (context as MainActivity).sourceManager.getWeatherSource(it)
        }

        val credits = mutableMapOf<String, String?>()
        credits["weather"] = if (location.forecastSource.isNotEmpty()) {
            distinctSources[location.forecastSource]?.supportedFeatures
                ?.getOrElse(SourceFeature.FORECAST) { null }
        } else {
            null
        }
        credits["current"] = if (!location.currentSource.isNullOrEmpty()) {
            distinctSources[location.currentSource]?.supportedFeatures
                ?.getOrElse(SourceFeature.CURRENT) { null }
        } else {
            null
        }
        credits["minutely"] = if (!location.minutelySource.isNullOrEmpty()) {
            distinctSources[location.minutelySource]?.supportedFeatures
                ?.getOrElse(SourceFeature.MINUTELY) { null }
        } else {
            null
        }
        credits["alert"] = if (!location.alertSource.isNullOrEmpty()) {
            distinctSources[location.alertSource]?.supportedFeatures
                ?.getOrElse(SourceFeature.ALERT) { null }
        } else {
            null
        }
        credits["airQuality"] = if (!location.airQualitySource.isNullOrEmpty()) {
            distinctSources[location.airQualitySource]?.supportedFeatures
                ?.getOrElse(SourceFeature.AIR_QUALITY) { null }
        } else {
            null
        }
        credits["pollen"] = if (!location.pollenSource.isNullOrEmpty()) {
            distinctSources[location.pollenSource]?.supportedFeatures
                ?.getOrElse(SourceFeature.POLLEN) { null }
        } else {
            null
        }
        credits["normals"] = if (!location.normalsSource.isNullOrEmpty()) {
            distinctSources[location.normalsSource]?.supportedFeatures
                ?.getOrElse(SourceFeature.NORMALS) { null }
        } else {
            null
        }

        val creditsText = StringBuilder()
        location.weather?.let { weather ->
            creditsText.append(
                context.getString(
                    R.string.weather_data_by,
                    credits["weather"] ?: context.getString(R.string.null_data_text)
                )
            )
            if (!credits["current"].isNullOrEmpty() && credits["current"] != credits["weather"]) {
                creditsText.append(
                    "\n" + context.getString(R.string.weather_current_data_by, credits["current"]!!)
                )
            }
            if (weather.minutelyForecast.isNotEmpty() &&
                !credits["minutely"].isNullOrEmpty() &&
                credits["minutely"] != credits["weather"]
            ) {
                creditsText.append(
                    "\n" + context.getString(R.string.weather_minutely_data_by, credits["minutely"]!!)
                )
            }
            if (weather.alertList.isNotEmpty() &&
                !credits["alert"].isNullOrEmpty() &&
                credits["alert"] != credits["weather"]
            ) {
                creditsText.append(
                    "\n" + context.getString(R.string.weather_alert_data_by, credits["alert"]!!)
                )
            }
            // Open-Meteo has a lengthy credits so we merge air quality and pollen identical credit in that case
            if (!credits["airQuality"].isNullOrEmpty() && credits["airQuality"] != credits["weather"]) {
                if (!credits["pollen"].isNullOrEmpty() && credits["pollen"] != credits["weather"]) {
                    if (credits["airQuality"] == credits["pollen"]) {
                        creditsText.append(
                            "\n" + context.getString(
                                R.string.weather_air_quality_and_pollen_data_by,
                                credits["airQuality"]!!
                            )
                        )
                    } else {
                        creditsText.append(
                            "\n" + context.getString(R.string.weather_air_quality_data_by, credits["airQuality"]!!) +
                                "\n" + context.getString(R.string.weather_pollen_data_by, credits["pollen"]!!)
                        )
                    }
                } else {
                    creditsText.append(
                        "\n" + context.getString(R.string.weather_air_quality_data_by, credits["airQuality"]!!)
                    )
                }
            } else {
                if (!credits["pollen"].isNullOrEmpty() && credits["pollen"] != credits["weather"]) {
                    creditsText.append(
                        "\n" + context.getString(R.string.weather_pollen_data_by, credits["pollen"]!!)
                    )
                }
            }
            if (weather.normals?.month != null &&
                !credits["normals"].isNullOrEmpty() &&
                credits["normals"] != credits["weather"]
            ) {
                creditsText.append(
                    "\n" + context.getString(R.string.weather_normals_data_by, credits["normals"]!!)
                )
            }
        }

        composeView.setContent {
            BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(context, location)) {
                ComposeView(
                    creditsText.toString(),
                    cardMarginsVertical.toInt()
                )
            }
        }
    }

    @Composable
    fun ComposeView(
        creditsText: String,
        cardMarginsVertical: Int,
    ) {
        var expand by remember { mutableStateOf(false) }

        val paddingTop = dimensionResource(R.dimen.little_margin) - cardMarginsVertical.dp
        Row(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = dimensionResource(R.dimen.normal_margin),
                        top = if (paddingTop > 0.dp) paddingTop else 0.dp,
                        end = dimensionResource(R.dimen.normal_margin),
                        bottom = dimensionResource(R.dimen.little_margin)
                    )
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpandableText(
                originalText = creditsText,
                expandAction = stringResource(R.string.action_see_more),
                expand = expand,
                color = Color.White,
                expandActionColor = Color.White,
                limitedMaxLines = 3,
                animationSpec = spring(),
                modifier = Modifier
                    .weight(1f)
                    .clickable { expand = !expand }
            )
        }
    }

    override fun getEnterAnimator(pendingAnimatorList: List<Animator>): Animator {
        return ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f).apply {
            duration = 450
            interpolator = FastOutSlowInInterpolator()
            startDelay = pendingAnimatorList.size * 150L
        }
    }
}
