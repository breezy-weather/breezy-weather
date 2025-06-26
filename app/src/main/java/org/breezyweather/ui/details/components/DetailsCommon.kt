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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.extensions.pxToDp
import org.breezyweather.common.extensions.spToPx
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.theme.compose.DayNightTheme
import kotlin.math.roundToInt

@Composable
fun DetailsItem(
    headlineText: String,
    supportingText: String?,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    withHelp: Boolean = false,
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(DayNightTheme.colors.titleColor)
            )
        }
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headlineText,
                    color = DayNightTheme.colors.titleColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (withHelp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        // TooltipBox already takes care of adding the info that there is a tooltip
                        contentDescription = null,
                        modifier = Modifier
                            .size(
                                context.pxToDp(
                                    context.spToPx(
                                        MaterialTheme.typography.bodyLarge.fontSize.value.roundToInt()
                                    ).roundToInt()
                                ).dp
                            )
                    )
                }
            }
            supportingText?.let {
                Text(text = it)
            }
        }
    }
}

@Composable
fun UnavailableChart(
    size: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.little_margin))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .clearAndSetSemantics {} // Chart is not read by screen readers, so just ignore the info message
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_about),
                contentDescription = null
            )
            Text(
                text = if (size == 0) {
                    stringResource(R.string.chart_no_hourly_data)
                } else {
                    stringResource(R.string.chart_not_enough_hourly_data)
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun NighttimeWithInfo(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tooltipState = rememberTooltipState()
    val coroutineScope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = {
            PlainTooltip { Text(stringResource(R.string.nighttime_details)) }
        },
        state = tooltipState
    ) {
        Row(
            modifier = modifier
                .clickable {
                    coroutineScope.launch {
                        tooltipState.show()
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.nighttime),
                style = MaterialTheme.typography.labelMedium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = null, // TooltipBox already takes care of adding the info that there is a tooltip
                modifier = Modifier
                    .size(
                        context.pxToDp(
                            context.spToPx(
                                MaterialTheme.typography.labelMedium.fontSize.value.roundToInt()
                            ).roundToInt()
                        ).dp
                    )
            )
        }
    }
}

@Composable
fun DetailsSectionHeader(
    sectionName: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = sectionName,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(
                    bottom = if (subtitle == null) dimensionResource(R.dimen.little_margin) else 0.dp
                )
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = DayNightTheme.colors.captionColor,
                modifier = Modifier
                    .padding(
                        bottom = dimensionResource(R.dimen.little_margin)
                    )
            )
        }
    }
}

@Composable
fun DetailsSectionDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier
            .padding(vertical = dimensionResource(R.dimen.normal_margin))
    )
}

@Composable
fun DetailsCardText(
    text1: String,
    text2: String? = null,
    modifier: Modifier = Modifier,
) {
    Material3CardListItem(
        withPadding = false,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.normal_margin),
                vertical = dimensionResource(R.dimen.little_margin)
            ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.normal_margin))
        ) {
            Text(text1)
            text2?.let {
                Text(it)
            }
        }
    }
}
