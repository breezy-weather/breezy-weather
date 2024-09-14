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

package org.breezyweather.settings.preference.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.breezyweather.R
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.defaultCardListItemElevation
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.themeRipple

@Composable
fun PreferenceView(
    @StringRes titleId: Int,
    @StringRes summaryId: Int? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    card: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(),
    onClick: () -> Unit,
) = PreferenceView(
    title = stringResource(titleId),
    summary = if (summaryId != null) stringResource(summaryId) else null,
    iconId = iconId,
    enabled = enabled,
    card = card,
    colors = colors,
    onClick = onClick
)

@Composable
fun PreferenceView(
    title: String,
    summary: String? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    card: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(),
    onClose: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val paddingValues = if (onClose == null) {
        PaddingValues(vertical = 8.dp)
    } else PaddingValues(bottom = 8.dp)
    // TODO: Redundant
    if (card) {
        Material3CardListItem(
            elevation = if (enabled) defaultCardListItemElevation else 0.dp
        ) {
            ListItem(
                tonalElevation = if (enabled) defaultCardListItemElevation else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else 0.5f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = themeRipple(),
                        onClick = onClick,
                        enabled = enabled,
                    )
                    .padding(paddingValues),
                leadingContent = if (iconId != null) {
                    {
                        Icon(
                            painter = painterResource(iconId),
                            tint = DayNightTheme.colors.titleColor,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else null,
                headlineContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = title,
                                color = DayNightTheme.colors.titleColor,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (onClose != null) {
                            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
                            IconButton(
                                onClick = {
                                    onClose()
                                },
                                modifier = Modifier.clip(CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.action_close),
                                    tint = DayNightTheme.colors.bodyColor
                                )
                            }
                        }
                    }
                },
                supportingContent = if (!summary.isNullOrEmpty()) {
                    {
                        Column {
                            if (onClose == null) { // We already have spacing from close button
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                            }
                            Text(
                                text = summary,
                                color = DayNightTheme.colors.bodyColor,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } else null
            )
        }
    } else {
        ListItem(
            colors = colors,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = themeRipple(),
                    onClick = onClick,
                    enabled = enabled,
                )
                .padding(paddingValues),
            leadingContent = if (iconId != null) {
                {
                    Icon(
                        painter = painterResource(iconId),
                        tint = DayNightTheme.colors.titleColor,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else null,
            headlineContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            color = DayNightTheme.colors.titleColor,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    if (onClose != null) {
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
                        IconButton(
                            onClick = {
                                onClose()
                            },
                            modifier = Modifier.clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_close),
                                tint = DayNightTheme.colors.bodyColor
                            )
                        }
                    }
                }
            },
            supportingContent = if (!summary.isNullOrEmpty()) {
                {
                    Column {
                        if (onClose == null) { // We already have spacing from close button
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                        }
                        Text(
                            text = summary,
                            color = DayNightTheme.colors.bodyColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else null
        )
    }
}
