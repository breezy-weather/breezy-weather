/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.basic.models.weather

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.common.extensions.format
import java.io.Serializable

/**
 * UV.
 */
class UV(
    val index: Float? = null
) : Serializable {

    val isValid: Boolean
        get() = index != null

    fun getLevel(context: Context) = when (index) {
        null -> null
        in 0f..UV_INDEX_LOW -> context.getString(R.string.uv_index_0_2)
        in UV_INDEX_LOW..UV_INDEX_MIDDLE -> context.getString(R.string.uv_index_3_5)
        in UV_INDEX_MIDDLE..UV_INDEX_HIGH -> context.getString(R.string.uv_index_6_7)
        in UV_INDEX_HIGH..UV_INDEX_EXCESSIVE -> context.getString(R.string.uv_index_8_10)
        in UV_INDEX_EXCESSIVE..Float.MAX_VALUE -> context.getString(R.string.uv_index_11)
        else -> null
    }

    fun getUVDescription(context: Context): String {
        val builder = StringBuilder()
        index?.let {
            builder.append(it.format(1))
        }
        getLevel(context)?.let {
            if (builder.toString().isNotEmpty()) builder.append(" ")
            builder.append(it)
        }
        return builder.toString()
    }

    fun getShortUVDescription(context: Context): String {
        val builder = StringBuilder()
        index?.let {
            builder.append(it.format(0))
        }
        getLevel(context)?.let {
            if (builder.toString().isNotEmpty()) builder.append(" ")
            builder.append(it)
        }
        return builder.toString()
    }

    companion object {
        const val UV_INDEX_LOW = 2f
        const val UV_INDEX_MIDDLE = 5f
        const val UV_INDEX_HIGH = 7f
        const val UV_INDEX_EXCESSIVE = 10f

        @ColorInt
        fun getUVColor(index: Float?, context: Context): Int {
            return if (index == null) {
                Color.TRANSPARENT
            } else when (index) {
                in 0f..UV_INDEX_LOW -> ContextCompat.getColor(context, R.color.colorLevel_1)
                in UV_INDEX_LOW..UV_INDEX_MIDDLE -> ContextCompat.getColor(context, R.color.colorLevel_2)
                in UV_INDEX_MIDDLE..UV_INDEX_HIGH -> ContextCompat.getColor(context, R.color.colorLevel_3)
                in UV_INDEX_HIGH..UV_INDEX_EXCESSIVE -> ContextCompat.getColor(context, R.color.colorLevel_4)
                in UV_INDEX_EXCESSIVE..Float.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_5)
                else -> Color.TRANSPARENT
            }
        }
    }
}
