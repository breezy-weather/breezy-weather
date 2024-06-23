package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper

data class AppBarState(
    val title: String = "",
    val actions: (@Composable RowScope.() -> Unit) = { AboutActivityIconButton(LocalContext.current) }
)

@Composable
fun AboutActivityIconButton(
    context: Context,
    modifier: Modifier = Modifier
) = IconButton(
    modifier = modifier,
    onClick = { IntentHelper.startAboutActivity(context) }
) {
    Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = stringResource(R.string.action_about),
        tint = MaterialTheme.colorScheme.onSurface,
    )
}
