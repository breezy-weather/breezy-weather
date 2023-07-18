package org.breezyweather.common.preference

import android.content.Context
import androidx.annotation.StringRes

class EditTextPreference(
    @StringRes override val titleId: Int,
    val summary: ((Context, String) -> String?)? = null,
    val content: String,
    val onValueChanged: (String) -> Unit,
) : Preference