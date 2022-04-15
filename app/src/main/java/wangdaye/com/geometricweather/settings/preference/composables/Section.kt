package wangdaye.com.geometricweather.settings.preference.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import wangdaye.com.geometricweather.R

@Composable
fun SectionHeader(
    title: String,
) {
    Box(modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun SectionFooter() {
    Divider(color = MaterialTheme.colorScheme.outline)
}