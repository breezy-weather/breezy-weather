package wangdaye.com.geometricweather.settings.preference.composables

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PreferenceScreen(
    content: LazyListScope.() -> Unit
) = LazyColumn(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
    content = content,
)