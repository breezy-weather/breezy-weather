package org.breezyweather.ui.daily.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.breezyweather.R
import org.breezyweather.ui.theme.compose.DayNightTheme

@Composable
fun DailyItem(
    headlineText: String,
    supportingText: String?,
    modifier: Modifier = Modifier,
    supportingContentDescription: String? = null,
    @DrawableRes icon: Int? = null,
) {
    if (icon != null) {
        // TODO: Remove paddings by making the Row and Column manually
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
                    text = headlineText,
                    color = DayNightTheme.colors.titleColor,
                    fontWeight = FontWeight.Bold
                )
            },
            supportingContent = supportingText?.let {
                {
                    Text(
                        text = it,
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
                text = headlineText,
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Bold
            )
            supportingText?.let {
                Text(
                    text = it,
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

@Composable
fun UnavailableChart(
    size: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = if (size == 0) {
            stringResource(R.string.chart_no_hourly_data)
        } else {
            stringResource(R.string.chart_not_enough_hourly_data)
        },
        fontStyle = FontStyle.Italic,
        modifier = modifier
    )
}
