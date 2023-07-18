package org.breezyweather.common.preference

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

class ListPreference(
    @StringRes override val titleId: Int,
    @ArrayRes val valueArrayId: Int,
    @ArrayRes val nameArrayId: Int,
    val selectedKey: String,
    val onValueChanged: (String) -> Unit
) : Preference