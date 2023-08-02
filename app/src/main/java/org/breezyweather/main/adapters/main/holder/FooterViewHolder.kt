package org.breezyweather.main.adapters.main.holder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.CallSuper
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import io.github.giangpham96.expandable_text_compose.ExpandableText
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.Source
import org.breezyweather.common.ui.composables.LocationPreference
import org.breezyweather.main.MainActivity
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.resource.providers.ResourceProvider

class FooterViewHolder(
    private val composeView: ComposeView
) : AbstractMainViewHolder(composeView) {

    @SuppressLint("SetTextI18n")
    @CallSuper
    fun onBindView(
        context: Context, location: Location, provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean,
        sourceManager: SourceManager
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val cardMarginsVertical = ThemeManager.getInstance(context)
            .weatherThemeDelegate
            .getHomeCardMargins(context).toFloat()

        val distinctSources = mutableMapOf<String, Source?>()
        listOf(
            location.weatherSource,
            location.airQualitySourceNotNull,
            location.allergenSourceNotNull,
            location.minutelySourceNotNull,
            location.alertSourceNotNull
        ).distinct().forEach {
            distinctSources[it] = sourceManager.getSource(it)
        }

        val credits = mutableMapOf<String, String?>()
        credits["weather"] = if (distinctSources[location.weatherSource] is MainWeatherSource) {
            (distinctSources[location.weatherSource] as MainWeatherSource).weatherAttribution
        } else null
        credits["minutely"] = if (distinctSources[location.minutelySource] is SecondaryWeatherSource
            && (distinctSources[location.minutelySource] as SecondaryWeatherSource).minutelyAttribution != credits["weather"]) {
            (distinctSources[location.minutelySource] as SecondaryWeatherSource).minutelyAttribution
        } else null
        credits["alert"] = if (distinctSources[location.alertSource] is SecondaryWeatherSource
            && (distinctSources[location.alertSource] as SecondaryWeatherSource).alertAttribution != credits["weather"]) {
            (distinctSources[location.alertSource] as SecondaryWeatherSource).alertAttribution
        } else null
        credits["airQuality"] = if (distinctSources[location.airQualitySource] is SecondaryWeatherSource
            && (distinctSources[location.airQualitySource] as SecondaryWeatherSource).airQualityAttribution != credits["weather"]) {
            (distinctSources[location.airQualitySource] as SecondaryWeatherSource).airQualityAttribution
        } else null
        credits["allergen"] = if (distinctSources[location.allergenSource] is SecondaryWeatherSource
            && (distinctSources[location.allergenSource] as SecondaryWeatherSource).allergenAttribution != credits["weather"]) {
            (distinctSources[location.allergenSource] as SecondaryWeatherSource).allergenAttribution
        } else null

        val creditsText = StringBuilder()
        if (location.weather != null) {
            creditsText.append(
                context.getString(R.string.weather_data_by)
                    .replace("$", credits["weather"] ?: context.getString(R.string.null_data_text))
            )
            if (location.weather.minutelyForecast.isNotEmpty()
                && !credits["minutely"].isNullOrEmpty()) {
                creditsText.append(
                    "\n" +
                    context.getString(R.string.weather_minutely_data_by)
                        .replace("$", credits["minutely"]!!)
                )
            }
            if (location.weather.alertList.isNotEmpty()
                && !credits["minutely"].isNullOrEmpty()) {
                creditsText.append(
                    "\n" +
                    context.getString(R.string.weather_alert_data_by)
                        .replace("$", credits["alert"]!!)
                )
            }
            // Open-Meteo has a lengthy credits so we merge air quality and allergen identical credit in that case
            if (!credits["airQuality"].isNullOrEmpty()) {
                if (!credits["allergen"].isNullOrEmpty()) {
                    if (credits["airQuality"] == credits["allergen"]) {
                        creditsText.append(
                            "\n" +
                            context.getString(R.string.weather_air_quality_and_allergen_data_by)
                                .replace("$", credits["airQuality"]!!)
                        )
                    } else {
                        creditsText.append(
                            "\n" +
                            context.getString(R.string.weather_air_quality_data_by)
                                .replace("$", credits["airQuality"]!!) +
                            "\n" +
                            context.getString(R.string.weather_allergen_data_by)
                                .replace("$", credits["allergen"]!!)
                        )
                    }
                } else {
                    creditsText.append(
                        "\n" +
                        context.getString(R.string.weather_air_quality_data_by)
                            .replace("$", credits["airQuality"]!!)
                    )
                }
            } else {
                if (!credits["allergen"].isNullOrEmpty()) {
                    creditsText.append(
                        "\n" +
                        context.getString(R.string.weather_allergen_data_by)
                            .replace("$", credits["allergen"]!!)
                    )
                }
            }
        }


        composeView.setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ComposeView((context as MainActivity), location, creditsText.toString(), cardMarginsVertical.toInt())
            }
        }
    }

    @Composable
    fun ComposeView(activity: MainActivity, location: Location, creditsText: String, cardMarginsVertical: Int) {
        var expand by remember { mutableStateOf(false) }
        var dialogOpenState by remember { mutableStateOf(false) }

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
            TextButton(
                onClick = {
                    dialogOpenState = true
                }
            ) {
                Text(
                    text = stringResource(R.string.action_edit),
                    color = Color.White,
                    fontSize = dimensionResource(id = R.dimen.content_text_size).value.sp
                )
            }
        }

        if (dialogOpenState) {
            AlertDialog(
                onDismissRequest = { dialogOpenState = false },
                title = {
                    Text(
                        text = stringResource(R.string.action_settings),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    LocationPreference(activity, location, true) {
                        dialogOpenState = false
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogOpenState = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_close),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }/*,
                dismissButton = if (true) { // If location list size > 1
                    {
                        Button(
                            onClick = {
                                //dialogOpenState = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.action_delete),
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                } else null*/
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
