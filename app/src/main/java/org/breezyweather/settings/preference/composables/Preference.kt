package org.breezyweather.settings.preference.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import org.breezyweather.theme.compose.rememberThemeRipple

@Composable
fun PreferenceView(
    @StringRes titleId: Int,
    @StringRes summaryId: Int? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = PreferenceView(
    title = stringResource(titleId),
    summary = if (summaryId != null) stringResource(summaryId) else null,
    iconId = iconId,
    enabled = enabled,
    onClick = onClick
)

@Composable
fun PreferenceView(
    title: String,
    summary: String? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    onClose: (() -> Unit)? = null,
    onClick: () -> Unit,
) = Material3CardListItem(
    elevation = if (enabled) defaultCardListItemElevation else 0.dp
) {
    val paddingValues = if (onClose == null) {
        PaddingValues(vertical = 8.dp)
    } else PaddingValues(bottom = 8.dp)
    ListItem(
        tonalElevation = if (enabled) defaultCardListItemElevation else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberThemeRipple(),
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
        supportingContent = if (summary?.isNotEmpty() == true) {
            {
                if (onClose == null) { // We already have spacing from close button
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                }
                Text(
                    text = summary,
                    color = DayNightTheme.colors.bodyColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else null
    )
}