package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper

@Composable
fun AboutActivityIconButton(
    context: Context,
    modifier: Modifier = Modifier,
) = IconButton(
    modifier = modifier,
    onClick = { IntentHelper.startAboutActivity(context) }
) {
    Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = stringResource(R.string.action_about),
        tint = MaterialTheme.colorScheme.onSurface
    )
}
