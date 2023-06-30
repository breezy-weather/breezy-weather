package org.breezyweather.common.basic.models.options.provider

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

enum class LocationProvider(
    override val id: String
): BaseEnum {

    BAIDU_IP("baidu_ip"),
    NATIVE("native");

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "baidu_ip" -> BAIDU_IP
            else -> NATIVE
        }
    }

    override val valueArrayId = R.array.location_service_values
    override val nameArrayId = R.array.location_services

    override fun getName(context: Context) = Utils.getName(context, this)
}