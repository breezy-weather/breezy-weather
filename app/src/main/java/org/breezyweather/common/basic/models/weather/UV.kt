package org.breezyweather.common.basic.models.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import org.breezyweather.R
import java.io.Serializable

/**
 * UV.
 */
class UV(
    val index: Int? = null,
    val level: String? = null,
    val description: String? = null
) : Serializable {

    companion object {
        const val UV_INDEX_LOW = 2
        const val UV_INDEX_MIDDLE = 5
        const val UV_INDEX_HIGH = 7
        const val UV_INDEX_EXCESSIVE = 10
    }

    val isValid: Boolean
        get() = index != null || level != null || description != null
    val isValidIndex: Boolean
        get() = index != null

    @get:SuppressLint("DefaultLocale")
    val uVDescription: String
        get() {
            val builder = StringBuilder()
            index?.let {
                builder.append(String.format("%d", it))
            }
            level?.let {
                if (builder.toString().isNotEmpty()) builder.append(" ")
                builder.append(it)
            }
            description?.let {
                if (builder.toString().isNotEmpty()) builder.append("\n")
                builder.append(it)
            }
            return builder.toString()
        }
    val shortUVDescription: String
        get() {
            val builder = StringBuilder()
            index?.let {
                builder.append(it)
            }
            level?.let {
                if (builder.toString().isNotEmpty()) builder.append(" ")
                builder.append(it)
            }
            return builder.toString()
        }

    @ColorInt
    fun getUVColor(context: Context): Int {
        return if (index == null) {
            Color.TRANSPARENT
        } else when (index) {
            in 0..UV_INDEX_LOW -> ContextCompat.getColor(context, R.color.colorLevel_1)
            in UV_INDEX_LOW..UV_INDEX_MIDDLE -> ContextCompat.getColor(context, R.color.colorLevel_2)
            in UV_INDEX_MIDDLE..UV_INDEX_HIGH -> ContextCompat.getColor(context, R.color.colorLevel_3)
            in UV_INDEX_HIGH..UV_INDEX_EXCESSIVE -> ContextCompat.getColor(context, R.color.colorLevel_4)
            in UV_INDEX_EXCESSIVE..Int.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_5)
            else -> Color.TRANSPARENT
        }
    }
}
