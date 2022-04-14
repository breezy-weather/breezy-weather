package wangdaye.com.geometricweather.settings.preference

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

fun generateListPreferenceModel(
    @StringRes titleId: Int,
    selectedKey: String,
    @ArrayRes valueArrayId: Int,
    @ArrayRes nameArrayId: Int,
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) = PreferenceModel.ListPreferenceModel(
    titleId = titleId,
    summaryGenerator = {context, value ->
        val values = context.resources.getStringArray(valueArrayId)
        val names = context.resources.getStringArray(nameArrayId)

        names[values.indexOfFirst { it == value }]
    },
    selectedKey = selectedKey,
    valueArrayId = valueArrayId,
    nameArrayId = nameArrayId,
    enabled = enabled,
    onValueChanged = onValueChanged,
)

