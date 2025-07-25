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

package org.breezyweather.ui.settings.preference.composables

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.breezyweather.R
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.common.widgets.defaultCardListItemElevation
import org.breezyweather.ui.theme.compose.themeRipple

@Composable
fun PreferenceViewWithCard(
    @StringRes titleId: Int,
    modifier: Modifier = Modifier,
    @StringRes summaryId: Int? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(),
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClick: () -> Unit,
) = PreferenceViewWithCard(
    title = stringResource(titleId),
    modifier = modifier,
    summary = if (summaryId != null) stringResource(summaryId) else null,
    iconId = iconId,
    enabled = enabled,
    colors = colors,
    isFirst = isFirst,
    isLast = isLast,
    onClick = onClick
)

@Composable
fun PreferenceView(
    @StringRes titleId: Int,
    modifier: Modifier = Modifier,
    @StringRes summaryId: Int? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(),
    onClick: () -> Unit,
) = PreferenceView(
    title = stringResource(titleId),
    modifier = modifier,
    summary = if (summaryId != null) stringResource(summaryId) else null,
    iconId = iconId,
    enabled = enabled,
    colors = colors,
    onClick = onClick
)

@Composable
fun PreferenceViewWithCard(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(),
    surface: Color = MaterialTheme.colorScheme.surface,
    onSurface: Color = MaterialTheme.colorScheme.onSurface,
    onClose: (() -> Unit)? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClick: () -> Unit,
) {
    Material3ExpressiveCardListItem(
        elevation = if (enabled) defaultCardListItemElevation else 0.dp,
        surface = surface,
        onSurface = onSurface,
        isFirst = isFirst,
        isLast = isLast,
        modifier = modifier
    ) {
        PreferenceView(
            title = title,
            summary = summary,
            iconId = iconId,
            enabled = enabled,
            card = true,
            colors = colors,
            onClose = onClose,
            onClick = onClick
        )
    }
}

@Composable
fun PreferenceView(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    card: Boolean = false,
    colors: ListItemColors = ListItemDefaults.colors(),
    onClose: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val paddingValues = if (onClose == null) {
        PaddingValues(vertical = 8.dp)
    } else {
        PaddingValues(bottom = 8.dp)
    }

    ListItem(
        colors = colors,
        tonalElevation = if (card && enabled) defaultCardListItemElevation else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = themeRipple(),
                onClick = onClick,
                enabled = enabled
            )
            .padding(paddingValues),
        leadingContent = if (iconId != null) {
            {
                Icon(
                    painter = painterResource(iconId),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            null
        },
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
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (onClose != null) {
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.small_margin)))
                    IconButton(
                        onClick = {
                            onClose()
                        },
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        supportingContent = if (!summary.isNullOrEmpty()) {
            {
                Column {
                    if (onClose == null) { // We already have spacing from close button
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
                    }
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            null
        }
    )
}
