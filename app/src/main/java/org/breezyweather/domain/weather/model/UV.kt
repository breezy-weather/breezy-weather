package org.breezyweather.domain.weather.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import breezyweather.domain.weather.model.UV
import org.breezyweather.R
import org.breezyweather.common.extensions.format

fun UV.getLevel(context: Context): String? {
    if (index == null) return null
    return when (index!!) {
        in 0.0..UV.UV_INDEX_LOW -> context.getString(R.string.uv_index_0_2)
        in UV.UV_INDEX_LOW..UV.UV_INDEX_MIDDLE -> context.getString(R.string.uv_index_3_5)
        in UV.UV_INDEX_MIDDLE..UV.UV_INDEX_HIGH -> context.getString(R.string.uv_index_6_7)
        in UV.UV_INDEX_HIGH..UV.UV_INDEX_EXCESSIVE -> context.getString(R.string.uv_index_8_10)
        in UV.UV_INDEX_EXCESSIVE..Double.MAX_VALUE -> context.getString(R.string.uv_index_11)
        else -> null
    }
}

fun UV.getUVDescription(context: Context): String {
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

fun UV.getShortUVDescription(context: Context): String {
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

@ColorInt
fun UV.getUVColor(context: Context): Int {
    if (index == null) return Color.TRANSPARENT
    return when (index!!) {
        in 0.0..UV.UV_INDEX_LOW -> ContextCompat.getColor(context, R.color.colorLevel_1)
        in UV.UV_INDEX_LOW..UV.UV_INDEX_MIDDLE -> ContextCompat.getColor(context, R.color.colorLevel_2)
        in UV.UV_INDEX_MIDDLE..UV.UV_INDEX_HIGH -> ContextCompat.getColor(context, R.color.colorLevel_3)
        in UV.UV_INDEX_HIGH..UV.UV_INDEX_EXCESSIVE -> ContextCompat.getColor(context, R.color.colorLevel_4)
        in UV.UV_INDEX_EXCESSIVE..Double.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_5)
        else -> Color.TRANSPARENT
    }
}