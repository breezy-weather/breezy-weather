package wangdaye.com.geometricweather.settings.preference

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.runtime.State

data class PreferenceSectionModel(
    @StringRes val sectionTitleId: Int?,
    val preferenceModelList: List<State<PreferenceModel>>
) {
    val itemCount: Int
        get() = preferenceModelList.size + (if (sectionTitleId == null) 0 else 2)
}

sealed class PreferenceModel(
    @StringRes open val titleId: Int,
    open val enabled: Boolean = true,
) {

    data class ClickablePreferenceModel(
        @StringRes override val titleId: Int,
        @StringRes val summaryId: Int?,
        override val enabled: Boolean = true,
        val onClick: () -> Unit
    ): PreferenceModel(
        titleId = titleId,
        enabled = enabled,
    )

    data class CheckboxPreferenceModel(
        @StringRes override val titleId: Int,
        val summaryGenerator: (Context, Boolean) -> String?,
        var checked: Boolean,
        override val enabled: Boolean = true,
        val onValueChanged: (Boolean) -> Unit,
    ): PreferenceModel(
        titleId = titleId,
        enabled = enabled,
    )

    data class ListPreferenceModel(
        @StringRes override val titleId: Int,
        val summaryGenerator: (Context, String) -> String?, // value -> summary.
        var selectedKey: String,
        @ArrayRes val valueArrayId: Int,
        @ArrayRes val nameArrayId: Int,
        override val enabled: Boolean = true,
        val onValueChanged: (String) -> Unit,
    ): PreferenceModel(
        titleId = titleId,
        enabled = enabled,
    )

    data class TimePickerPreferenceModel(
        @StringRes override val titleId: Int,
        val summaryGenerator: (Context, String) -> String?, // currentTime (xx:xx) -> summary.
        var currentTime: String,
        override val enabled: Boolean = true,
        val onValueChanged: (String) -> Unit,
    ): PreferenceModel(
        titleId = titleId,
        enabled = enabled,
    )

    data class EditTextPreferenceModel(
        @StringRes override val titleId: Int,
        val summaryGenerator: (Context, String) -> String?, // content -> summary.
        var content: String,
        override val enabled: Boolean = true,
        val onValueChanged: (String) -> Unit,
    ): PreferenceModel(
        titleId = titleId,
        enabled = enabled,
    )
}