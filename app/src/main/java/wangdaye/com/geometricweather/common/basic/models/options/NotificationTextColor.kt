package wangdaye.com.geometricweather.common.basic.models.options

import android.content.Context
import androidx.annotation.ColorRes
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.BaseEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils

enum class NotificationTextColor(
    override val id: String,
    @ColorRes val mainTextColorResId: Int,
    @ColorRes val subTextColorResId: Int
): BaseEnum {

    DARK("dark", R.color.colorTextDark, R.color.colorTextDark2nd),
    GREY("grey", R.color.colorTextGrey, R.color.colorTextGrey2nd),
    LIGHT("light", R.color.colorTextLight, R.color.colorTextLight2nd);

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "light" -> LIGHT
            "grey" -> GREY
            else -> DARK
        }
    }

    override val valueArrayId = R.array.notification_text_color_values
    override val nameArrayId = R.array.notification_text_colors

    override fun getName(context: Context) = Utils.getName(context, this)
}