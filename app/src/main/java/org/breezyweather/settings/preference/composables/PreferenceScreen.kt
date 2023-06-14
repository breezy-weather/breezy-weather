package org.breezyweather.settings.preference.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    content: LazyListScope.() -> Unit
) = LazyColumn(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
    contentPadding = paddingValues,
    content = content,
)