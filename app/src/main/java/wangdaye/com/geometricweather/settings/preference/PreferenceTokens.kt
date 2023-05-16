package wangdaye.com.geometricweather.settings.preference

import androidx.annotation.StringRes

sealed class PreferenceToken {

    open val preferenceKey: String
        get() = ""

    class SectionHeader(
        @StringRes val sectionTitleId: Int
    ): PreferenceToken() {
        override val preferenceKey: String
            get() = "header_$sectionTitleId"
    }

    class SectionFooter(
        @StringRes val sectionTitleId: Int
    ): PreferenceToken() {
        override val preferenceKey: String
            get() = "footer_$sectionTitleId"
    }

    class BottomInset : PreferenceToken() {

        override val preferenceKey: String
            get() = "bottom_inset_${hashCode()}"

        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    class ClickablePreference(
        @StringRes val titleId: Int
    ): PreferenceToken() {
        override val preferenceKey: String
            get() = "pref_$titleId"
    }

    class SwitchPreference(
        @StringRes val titleId: Int
    ): PreferenceToken() {
        override val preferenceKey: String
            get() = "chbx_$titleId"
    }

    class ListPreference(
        @StringRes val titleId: Int
    ): PreferenceToken() {
        override val preferenceKey: String
            get() = "list_$titleId"
    }

    class TimePickerPreference(
        @StringRes val titleId: Int
    ): PreferenceToken() {
        override val preferenceKey: String
            get() = "tpkr_$titleId"
    }

    class EditTextPreference(
        @StringRes val titleId: Int
    ): PreferenceToken() {
        override val preferenceKey: String
            get() = "edtx_$titleId"
    }
}