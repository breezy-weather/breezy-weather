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

package org.breezyweather.ui.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.UV
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.weather.model.getLevel
import org.breezyweather.domain.weather.model.getUVColor
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DetailsUV(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    defaultValue: Pair<Date, UV>?,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.uV?.isValid == true }
            .associate { it.date.time to it.uV!! }
            .toImmutableMap()
    }
    var activeItem: Pair<Date, UV>? by remember { mutableStateOf(null) }
    val markerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeItem = targets.firstOrNull()?.let { target ->
                    mappedValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeItem = null
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        item {
            UVHeader(location, daily, activeItem, defaultValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                UVChart(location, mappedValues, daily, markerVisibilityListener)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        /*if (dayUV?.isValid == true) {
            // TODO: UV description of the maximum value
            // TODO: Sun protection recommended from XX:XX to YY:YY
        }*/
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.uv_index_about))
        }
        item {
            DetailsCardText(stringResource(R.string.uv_index_about_description))
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.uv_index_scale))
        }
        item {
            UVScale()
        }
        bottomInsetItem()
    }
}

@Composable
fun UVHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, UV>?,
    defaultValue: Pair<Date, UV>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        UVItem(
            header = activeItem.first.getFormattedTime(location, context, context.is12Hour),
            uV = activeItem.second
        )
    } else if (daily.uV?.isValid == true) {
        UVItem(
            header = stringResource(R.string.uv_index_maximum_value),
            uV = daily.uV
        )
    } else {
        UVItem(
            header = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour),
            uV = defaultValue?.second
        )
    }
}

@Composable
private fun UVItem(
    header: String?,
    uV: UV?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Icon(
            modifier = Modifier
                .size(dimensionResource(R.dimen.material_icon_size)),
            painter = painterResource(R.drawable.ic_circle),
            contentDescription = null,
            tint = uV?.getUVColor(context)?.let { Color(it) } ?: Color.Transparent
        )
        Column {
            TextFixedHeight(
                text = header ?: "",
                style = MaterialTheme.typography.labelMedium
            )
            TextFixedHeight(
                text = buildAnnotatedString {
                    uV?.index?.let {
                        append(UnitUtils.formatDouble(context, it, 1))
                        append(" ")
                    }
                    uV?.getLevel(context)?.let {
                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            append(it)
                        }
                    }
                },
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
private fun UVChart(
    location: Location,
    mappedValues: ImmutableMap<Long, UV>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val maxY = remember(mappedValues) {
        max(
            UV.UV_INDEX_HIGH,
            // TODO: Make this a const:
            (mappedValues.values.maxOfOrNull { it.index ?: 0.0 } ?: 0.0).roundToInt().plus(1.0)
        )
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { it.index ?: 0 }
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        daily.date,
        maxY,
        { _, value, _ -> UnitUtils.formatInt(context, value.roundToInt()) },
        persistentListOf(
            persistentMapOf(
                19f to Color(255, 255, 255),
                11f to colorResource(R.color.colorLevel_5),
                10f to colorResource(R.color.colorLevel_4),
                7f to colorResource(R.color.colorLevel_3),
                5f to colorResource(R.color.colorLevel_2),
                2f to colorResource(R.color.colorLevel_1),
                0f to Color(110, 110, 110)
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.index?.roundToInt()
                ?.let { UnitUtils.formatInt(context, it) }
                ?: "-"
        },
        trendHorizontalLines = persistentMapOf(
            UV.UV_INDEX_MIDDLE to context.getString(R.string.uv_alert_level)
        ),
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ 1.0 }) // Every rounded UVI
        },
        markerVisibilityListener = markerVisibilityListener
    )
}

// TODO: Accessibility
@Composable
fun UVScale(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Material3ExpressiveCardListItem(
        modifier = modifier,
        isFirst = true,
        isLast = true
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.normal_margin),
                vertical = dimensionResource(R.dimen.small_margin)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.uv_index_description),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    stringResource(R.string.uv_index),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            UV.uvThresholds.forEachIndexed { index, startingValue ->
                val endingValue = UV.uvThresholds.getOrElse(index + 1) { null }
                    ?.let { " – ${UnitUtils.formatInt(context, it - 1)}" }
                    ?: "+"
                val uv = UV(index = startingValue.toDouble())
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.small_margin))
                ) {
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin))
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.material_icon_size)),
                            painter = painterResource(R.drawable.ic_circle),
                            contentDescription = null,
                            tint = Color(uv.getUVColor(context))
                        )
                        Text(
                            text = uv.getLevel(context)!!
                        )
                    }
                    Text(
                        "${UnitUtils.formatInt(context, startingValue)}$endingValue",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
