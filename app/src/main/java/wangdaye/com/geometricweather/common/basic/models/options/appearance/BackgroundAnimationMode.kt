package wangdaye.com.geometricweather.common.basic.models.options.appearance

import android.content.Context
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.BaseEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils

enum class BackgroundAnimationMode(
    override val id: String
): BaseEnum {

    SYSTEM("system"),
    ENABLED("enabled"),
    DISABLED("disabled");

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "enabled" -> ENABLED
            "disabled" -> DISABLED
            else -> SYSTEM
        }
    }

    override val valueArrayId = R.array.background_animation_values
    override val nameArrayId = R.array.background_animation

    override fun getName(context: Context) = Utils.getName(context, this)
}