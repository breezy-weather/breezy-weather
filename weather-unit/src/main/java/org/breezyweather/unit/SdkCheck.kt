/*
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

package org.breezyweather.unit

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun supportsNumberFormatter() = getVersionSdkInt() >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun supportsNumberFormatterUsage() = getVersionSdkInt() >= Build.VERSION_CODES.TIRAMISU

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun supportsNumberFormat() = getVersionSdkInt() >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun supportsMeasureFormat() = getVersionSdkInt() >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
fun supportsMeasureFormatPerUnit() = getVersionSdkInt() >= Build.VERSION_CODES.P

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun supportsMeasureUnit() = getVersionSdkInt() >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun supportsMeasureUnitAtmosphere() = getVersionSdkInt() >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.BAKLAVA)
fun supportsMeasureUnitBeaufort() = getVersionSdkInt() >= Build.VERSION_CODES.BAKLAVA

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun supportsMeasureUnitKnot() = getVersionSdkInt() >= Build.VERSION_CODES.O

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun supportsMeasureUnitPercent() = getVersionSdkInt() >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun supportsMeasureUnitPermille() = getVersionSdkInt() >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
fun supportsUnitDisplayName() = getVersionSdkInt() >= Build.VERSION_CODES.P

fun getVersionSdkInt(): Int {
    return Build.VERSION.SDK_INT
}
