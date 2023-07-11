package org.breezyweather.common.ui.composables

import androidx.compose.runtime.Composable
import org.breezyweather.R
import org.breezyweather.settings.preference.composables.PreferenceView

@Composable
fun NotificationCard(
    title: String,
    summary: String,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    PreferenceView(
        iconId = R.drawable.ic_notifications,
        onClick = onClick,
        title = title,
        summary = summary,
        onClose = onClose
    )
}