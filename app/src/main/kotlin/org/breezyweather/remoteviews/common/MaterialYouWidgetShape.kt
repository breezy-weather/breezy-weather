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

package org.breezyweather.remoteviews.common

import org.breezyweather.R

/**
 * Not yet used
 * Contains the shapes of the Material You - Forecast
 */
enum class MaterialYouWidgetShape(
    val defaultWidth: Int,
    val defaultHeight: Int,
    val miniWidth: Int,
    val miniHeight: Int,
    // val layout: Int,
) {
    MINI(
        R.dimen.widget_material_you_mini_default_width,
        R.dimen.widget_material_you_mini_default_height,
        R.dimen.widget_material_you_minimum_width_for_square_mini,
        R.dimen.widget_material_you_minimum_height_for_square_mini
        // R.layout.widget_material_you_mini
    ),
    SQUARE(
        R.dimen.widget_material_you_default_size,
        R.dimen.widget_material_you_default_size,
        R.dimen.widget_material_you_minimum_size_for_square,
        R.dimen.widget_material_you_minimum_size_for_square
        // R.layout.widget_material_you
    ),
    MEDIUM(
        R.dimen.widget_material_you_medium_default_width,
        R.dimen.widget_material_you_medium_default_height,
        R.dimen.widget_material_you_minimum_width_for_square_medium,
        R.dimen.widget_material_you_minimum_height_for_square_medium
        // R.layout.widget_material_you_medium
    ),
    LARGE(
        R.dimen.widget_material_you_large_default_width,
        R.dimen.widget_material_you_large_default_height,
        R.dimen.widget_material_you_minimum_width_for_square_large,
        R.dimen.widget_material_you_minimum_height_for_square_large
        // R.layout.widget_material_you_medium
    ),
}
