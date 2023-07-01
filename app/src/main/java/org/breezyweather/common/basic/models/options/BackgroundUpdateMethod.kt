package org.breezyweather.common.basic.models.options

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

enum class BackgroundUpdateMethod(
    override val id: String
): BaseEnum {

    WORKER("worker"),
    NOTIFICATION("notification");

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "notification" -> NOTIFICATION
            else -> WORKER
        }
    }

    override val valueArrayId = R.array.background_update_method_values
    override val nameArrayId = R.array.background_update_methods

    override fun getName(context: Context) = Utils.getName(context, this)
}