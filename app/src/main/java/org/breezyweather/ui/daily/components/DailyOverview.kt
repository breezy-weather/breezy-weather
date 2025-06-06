package org.breezyweather.ui.daily.components

import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.AnimatableIconView
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory

@Composable
fun DailyOverview(
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        daily.day?.let { day ->
            dailyHalfDay(day, true)
        }
        daily.night?.let { night ->
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            dailyHalfDay(night, false)
        }
    }
}

@Composable
fun DailyTitle(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    style: TextStyle = MaterialTheme.typography.titleSmall,
) {
    if (icon != null) {
        ListItem(
            leadingContent = {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            },
            headlineContent = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.primary,
                    style = style,
                    modifier = modifier
                )
            }
        )
    } else {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = style,
            modifier = modifier
        )
    }
}

fun LazyListScope.dailyHalfDay(
    halfDay: HalfDay,
    isDaytime: Boolean,
) {
    item {
        DailyTitle(
            text = stringResource(if (isDaytime) R.string.daytime else R.string.nighttime),
            style = MaterialTheme.typography.titleMedium
        )
    }
    item {
        DailyOverview(
            halfDay = halfDay,
            isDaytime = isDaytime
        )
    }
    item {
        HorizontalDivider()
    }
}

@Composable
fun DailyOverview(
    halfDay: HalfDay,
    isDaytime: Boolean,
    modifier: Modifier = Modifier,
) {
    val builder = StringBuilder()
    val contentDescription = StringBuilder()
    val context = LocalContext.current
    val temperatureUnit: TemperatureUnit = SettingsManager.getInstance(context).temperatureUnit
    if (!halfDay.weatherText.isNullOrEmpty()) {
        builder.append(halfDay.weatherText)
        contentDescription.append(halfDay.weatherText)
    }
    halfDay.temperature?.temperature?.let {
        if (builder.toString().isNotEmpty()) {
            builder.append(context.getString(R.string.comma_separator))
            contentDescription.append(context.getString(R.string.comma_separator))
        }
        builder.append(temperatureUnit.getValueText(context, it))
        contentDescription.append(temperatureUnit.getValueVoice(context, it))
    }
    ListItem(
        leadingContent = if (halfDay.weatherCode != null) {
            val provider = ResourcesProviderFactory.newInstance
            {
                AndroidView(
                    factory = {
                        AnimatableIconView(context).apply {
                            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                            setAnimatableIcon(
                                provider.getWeatherIcons(halfDay.weatherCode, isDaytime),
                                provider.getWeatherAnimators(halfDay.weatherCode, isDaytime)
                            )
                            setOnClickListener {
                                startAnimators()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.little_weather_icon_size))
                )
            }
        } else {
            null
        },
        headlineContent = {
            Text(
                text = builder.toString(),
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clearAndSetSemantics {
                    this.contentDescription = contentDescription.toString()
                }
            )
        },
        modifier = modifier
    )
}

@Composable
fun DailyItem(
    headlineText: String,
    supportingText: String?,
    modifier: Modifier = Modifier,
    supportingContentDescription: String? = null,
    @DrawableRes icon: Int? = null,
) {
    if (icon != null) {
        ListItem(
            leadingContent = {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(DayNightTheme.colors.titleColor)
                )
            },
            headlineContent = {
                Text(
                    text = headlineText
                )
            },
            supportingContent = supportingText?.let {
                {
                    Text(
                        text = it,
                        color = DayNightTheme.colors.titleColor,
                        fontWeight = FontWeight.Black,
                        modifier = if (supportingContentDescription != null) {
                            Modifier.clearAndSetSemantics {
                                contentDescription = supportingContentDescription
                            }
                        } else {
                            Modifier
                        }
                    )
                }
            },
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier
        ) {
            Text(
                text = headlineText
            )
            supportingText?.let {
                Text(
                    text = it,
                    color = DayNightTheme.colors.titleColor,
                    fontWeight = FontWeight.Black,
                    modifier = if (supportingContentDescription != null) {
                        Modifier.clearAndSetSemantics {
                            contentDescription = supportingContentDescription
                        }
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}
