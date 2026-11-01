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

package org.breezyweather.ui.main.compose

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.extensions.DEFAULT_CARD_LIST_ITEM_ELEVATION_DP
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.domain.location.model.getLocationListSubtitle
import org.breezyweather.domain.location.model.getPlace

@Composable
fun LocationCard(
    location: Location,
    isSelected: Boolean,
    weatherIcon: Drawable?,
    onClick: () -> Unit,
    onSwipeTowardStart: () -> Unit,
    onSwipeTowardEnd: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeTowardEnd()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeTowardStart()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    val elevatedSurface = ColorUtils.getWidgetSurfaceColor(
        DEFAULT_CARD_LIST_ITEM_ELEVATION_DP,
        context.getThemeColor(androidx.appcompat.R.attr.colorPrimary),
        context.getThemeColor(com.google.android.material.R.attr.colorSurface)
    )
    val itemBackground = if (isSelected) {
        context.getThemeColor(com.google.android.material.R.attr.colorPrimaryContainer)
    } else if (!location.weather?.currentAlertList.isNullOrEmpty()) {
        ContextCompat.getColor(context, R.color.alert_background)
    } else {
        elevatedSurface
    }
    val startBg = context.getThemeColor(com.google.android.material.R.attr.colorErrorContainer)
    val startTint = context.getThemeColor(com.google.android.material.R.attr.colorOnErrorContainer)
    val endBg = if (location.isCurrentPosition) {
        context.getThemeColor(com.google.android.material.R.attr.colorTertiaryContainer)
    } else {
        context.getThemeColor(com.google.android.material.R.attr.colorSecondaryContainer)
    }
    val endTint = if (location.isCurrentPosition) {
        context.getThemeColor(com.google.android.material.R.attr.colorOnTertiaryContainer)
    } else {
        context.getThemeColor(com.google.android.material.R.attr.colorOnSecondaryContainer)
    }
    val endIcon = R.drawable.ic_settings

    val talkBack = remember(location) {
        buildString {
            if (location.isCurrentPosition) {
                append(context.getString(R.string.location_current))
            }
            if (toString().isNotEmpty()) {
                append(context.getString(org.breezyweather.unit.R.string.locale_separator))
            }
            append(", ").append(
                context.getString(R.string.location_swipe_to_delete)
            )
        }
    }

    val corner = 50 // percent

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier
            /*.padding(
                start = dimensionResource(R.dimen.small_margin),
                end = dimensionResource(R.dimen.small_margin),
                bottom = dimensionResource(R.dimen.small_margin),
            )*/
            .semantics { contentDescription = talkBack }
            /*.then(
                if (isSelected) {
                    Modifier.border(4.dp, Color(elevatedSurface), RoundedCornerShape(corner))
                } else {
                    Modifier
                }
            )*/
            .clip(RoundedCornerShape(corner)),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bg = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(startBg)
                SwipeToDismissBoxValue.EndToStart -> Color(endBg)
                else -> Color.Transparent
            }
            val iconRes = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> R.drawable.ic_delete
                SwipeToDismissBoxValue.EndToStart -> endIcon
                else -> R.drawable.ic_delete
            }
            val tint = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(startTint)
                SwipeToDismissBoxValue.EndToStart -> Color(endTint)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = dimensionResource(R.dimen.normal_margin)),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = tint
                )
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                Color(itemBackground),
                                RoundedCornerShape(corner)
                            )
                        } else {
                            Modifier.background(Color(itemBackground))
                        }
                    )
                    .clickable(onClick = onClick)
                    .padding(end = dimensionResource(R.dimen.normal_margin)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_drag),
                    contentDescription = stringResource(R.string.settings_items_drag_to_sort),
                    tint = Color(context.getThemeColor(androidx.appcompat.R.attr.colorPrimary)),
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.small_margin))
                        .size(dimensionResource(R.dimen.material_icon_size))
                        .pointerInput(location.formattedId) {
                            detectDragGestures(
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount.y)
                                }
                            )
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = dimensionResource(R.dimen.normal_margin)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (weatherIcon != null) {
                            val bitmap = remember(weatherIcon) {
                                weatherIcon.toBitmap().asImageBitmap()
                            }
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    dimensionResource(R.dimen.small_weather_icon_size)
                                )
                            )
                            Spacer(Modifier.width(dimensionResource(R.dimen.small_margin)))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = (if (location.isCurrentPosition) "⊙ " else "") + location.getPlace(context),
                                color = Color(
                                    context.getThemeColor(
                                        if (isSelected) {
                                            com.google.android.material.R.attr.colorOnPrimaryContainer
                                        } else {
                                            context.getThemeColor(R.attr.colorTitleText)
                                        }
                                    )
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = dimensionResource(R.dimen.title_text_size).value.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = location.getLocationListSubtitle(context),
                                color = Color(
                                    if (!location.weather?.currentAlertList.isNullOrEmpty()) {
                                        ContextCompat.getColor(context, R.color.alert_text)
                                    } else {
                                        context.getThemeColor(R.attr.colorBodyText)
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = dimensionResource(R.dimen.content_text_size).value.sp
                            )
                        }
                    }
                }
            }
        }
    )
}
