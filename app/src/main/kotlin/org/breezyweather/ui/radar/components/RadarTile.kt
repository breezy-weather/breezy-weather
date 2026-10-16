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

package org.breezyweather.ui.radar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import breezyweather.domain.radar.model.RadarInfo
import breezyweather.domain.radar.model.RadarUrl
import org.breezyweather.R
import org.breezyweather.common.extensions.formatPercent
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.radar.INITIAL_ZOOM_LEVEL
import org.breezyweather.ui.radar.RadarUiState
import org.breezyweather.ui.settings.preference.composables.ListPreferenceViewWithCard
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import java.util.Date
import kotlin.math.roundToInt

@Composable
internal fun RadarTile(
    radarUiState: RadarUiState,
    dialogSettingsOpenState: Boolean,
    closeSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseStyle = remember {
        // TODO: Configurable
        BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty")
    }
    val camera = rememberCameraState(
        firstPosition = CameraPosition(target = Position(latitude = 0.0, longitude = 0.0), zoom = 1.0)
    )

    var radarInfo by remember { mutableStateOf<Map<String, RadarInfo>>(emptyMap()) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedColorScheme by remember { mutableStateOf<String?>(null) }

    var opacity by remember { mutableFloatStateOf(0.8f) }

    var sliderIndex by remember { mutableFloatStateOf(0f) }
    var sliderValues by remember { mutableStateOf<List<RadarUrl>>(emptyList()) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        sliderIndex = 0f
        radarUiState.wrapper?.info?.let { info ->
            radarInfo = info
            selectedType = info.keys.firstOrNull() // TODO: Should look into saved config, and fallback to first
            if (selectedType != null) {
                // TODO: Should look into saved config, and fallback to first
                selectedColorScheme = if (!radarInfo[selectedType]!!.colorSchemes.isNullOrEmpty()) {
                    radarInfo[selectedType]!!.colorSchemes!!.first().replacePath
                } else {
                    null
                }
                sliderValues = if (selectedColorScheme != null) {
                    radarInfo[selectedType]!!.url
                        .map { url ->
                            url.copy(url = url.url.replace("\$color", selectedColorScheme!!))
                        }
                } else {
                    radarInfo[selectedType]!!.url
                }
                // Position to most recent image
                sliderIndex = info[selectedType]!!.url
                    .indexOfLast { it.time < Date() }
                    .takeIf { it != -1 }?.toFloat()
                    ?: 0f
            } else {
                sliderValues = emptyList()
                sliderIndex = 0f
            }
        }
    }

    LaunchedEffect(radarUiState.location) {
        // re-setting the status bar color once the location is fetched
        if (radarUiState.location != null) {
            camera.animateTo(
                finalPosition = camera.position.copy(
                    target = Position(
                        latitude = radarUiState.location.latitude,
                        longitude = radarUiState.location.longitude
                    ),
                    zoom = INITIAL_ZOOM_LEVEL.toDouble()
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column {
            if (radarInfo.size > 1 && selectedType != null) {
                ButtonGroup(
                    modifier = Modifier.padding(
                        start = dimensionResource(R.dimen.normal_margin),
                        end = dimensionResource(R.dimen.normal_margin),
                        bottom = dimensionResource(R.dimen.normal_margin)
                    ),
                    overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                    },
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    radarInfo.keys.forEach { key ->
                        toggleableItem(
                            checked = selectedType == key,
                            onCheckedChange = {
                                selectedType = key
                                sliderValues = if (selectedColorScheme != null) {
                                    radarInfo[selectedType]!!.url
                                        .map { url ->
                                            url.copy(url = url.url.replace("\$color", selectedColorScheme!!))
                                        }
                                } else {
                                    radarInfo[selectedType]!!.url
                                }
                                // Position to most recent image
                                sliderIndex = radarInfo[selectedType]!!.url
                                    .indexOfLast { url -> url.time < Date() }
                                    .takeIf { index -> index != -1 }?.toFloat()
                                    ?: 0f
                            },
                            label = radarInfo[key]!!.name
                        )
                    }
                }
            }
            MaplibreMap(
                modifier = Modifier.fillMaxWidth().weight(1f),
                baseStyle = baseStyle,
                cameraState = camera
            ) {
                if (sliderValues.isNotEmpty()) {
                    RasterLayer(
                        id = "radar",
                        source = rememberRasterSource(
                            tiles = listOf(sliderValues[sliderIndex.roundToInt()].url),
                            tileSize = 256
                        ),
                        opacity = const(opacity)
                    )
                }
            }
        }
        if (sliderValues.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.normal_margin))
                    .padding(bottom = dimensionResource(R.dimen.large_margin))
                    .align(Alignment.BottomEnd)
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.normal_margin))
            ) {
                Material3ExpressiveCardListItem(
                    isFirst = true,
                    isLast = true,
                    modifier = Modifier.fillMaxHeight()
                        .align(Alignment.CenterVertically)
                ) {
                    IconButton(
                        modifier = Modifier.fillMaxHeight(),
                        onClick = {
                            // TODO: Add Play/Pause feature
                        }
                    ) {
                        Icon(
                            Icons.Default.PlayArrow, // TODO: Icons.Default.Pause
                            contentDescription = "", // TODO: stringResource(R.string.action_play),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Material3ExpressiveCardListItem(
                    isFirst = true,
                    isLast = true
                ) {
                    Column(
                        modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
                    ) {
                        Slider(
                            value = sliderIndex,
                            onValueChange = { newValue ->
                                sliderIndex = newValue
                            },
                            steps = sliderValues.size - 2,
                            valueRange = 0f..(sliderValues.size - 1).toFloat()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                dimensionResource(R.dimen.normal_margin)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!sliderValues[sliderIndex.roundToInt()].label.isNullOrEmpty()) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = sliderValues[sliderIndex.roundToInt()].label!!,
                                    textAlign = TextAlign.Left
                                )
                            }
                            Text(
                                text = sliderValues[sliderIndex.roundToInt()].time
                                    .getFormattedTime(radarUiState.location, context, context.is12Hour),
                                textAlign = if (sliderValues[sliderIndex.roundToInt()].label.isNullOrEmpty()) {
                                    TextAlign.Center
                                } else {
                                    TextAlign.Right
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (dialogSettingsOpenState) {
        AlertDialogNoPadding(
            onDismissRequest = {
                closeSettings()
            },
            title = {
                Text(
                    text = stringResource(R.string.action_settings),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    if (!radarInfo[selectedType]?.colorSchemes.isNullOrEmpty()) {
                        Column(
                            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
                        ) {
                            ListPreferenceViewWithCard(
                                title = "Color scheme", // TODO: stringRessource()
                                summary = { _, value ->
                                    radarInfo[selectedType]!!.colorSchemes!!
                                        .firstOrNull { cs -> cs.replacePath == value }?.name
                                        ?: "Unknown"
                                },
                                selectedKey = selectedColorScheme!!,
                                valueArray = radarInfo[selectedType]!!.colorSchemes!!
                                    .map { value -> value.replacePath }.toTypedArray(),
                                nameArray = radarInfo[selectedType]!!.colorSchemes!!
                                    .map { value -> value.name }.toTypedArray(),
                                withState = false,
                                isFirst = true,
                                isLast = true,
                                onValueChanged = { newValue ->
                                    selectedColorScheme = newValue
                                    sliderValues = radarInfo[selectedType]!!.url
                                        .map { url ->
                                            url.copy(url = url.url.replace("\$color", newValue))
                                        }
                                }
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
                    ) {
                        Slider(
                            value = opacity,
                            onValueChange = { newValue ->
                                opacity = newValue
                            },
                            steps = 9,
                            valueRange = 0f..1f
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = opacity.toDouble().fraction.formatPercent(context),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        closeSettings()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_close),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}
