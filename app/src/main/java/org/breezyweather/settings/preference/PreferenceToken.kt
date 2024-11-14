/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.settings.preference

import androidx.annotation.StringRes

sealed class PreferenceToken {

    open val preferenceKey: String
        get() = ""

    class SectionHeader(
        @StringRes val sectionTitleId: Int,
    ) : PreferenceToken() {
        override val preferenceKey: String
            get() = "header_$sectionTitleId"
    }

    class SectionFooter(
        @StringRes val sectionTitleId: Int,
    ) : PreferenceToken() {
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
        @StringRes val titleId: Int,
    ) : PreferenceToken() {
        override val preferenceKey: String
            get() = "pref_$titleId"
    }

    class SwitchPreference(
        @StringRes val titleId: Int,
    ) : PreferenceToken() {
        override val preferenceKey: String
            get() = "chbx_$titleId"
    }

    class ListPreference(
        @StringRes val titleId: Int,
    ) : PreferenceToken() {
        override val preferenceKey: String
            get() = "list_$titleId"
    }

    class TimePickerPreference(
        @StringRes val titleId: Int,
    ) : PreferenceToken() {
        override val preferenceKey: String
            get() = "tpkr_$titleId"
    }

    class EditTextPreference(
        @StringRes val titleId: Int,
    ) : PreferenceToken() {
        override val preferenceKey: String
            get() = "edtx_$titleId"
    }
}
