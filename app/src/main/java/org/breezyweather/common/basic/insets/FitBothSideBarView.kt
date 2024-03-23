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

package org.breezyweather.common.basic.insets

import androidx.annotation.IntDef

interface FitBothSideBarView {
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(SIDE_TOP, SIDE_BOTTOM)
    annotation class FitSide

    fun addFitSide(@FitSide side: Int)
    fun removeFitSide(@FitSide side: Int)
    fun setFitSystemBarEnabled(top: Boolean, bottom: Boolean)
    val topWindowInset: Int
    val bottomWindowInset: Int

    companion object {
        const val SIDE_TOP = 1
        const val SIDE_BOTTOM = 1 shl 1
    }
}
