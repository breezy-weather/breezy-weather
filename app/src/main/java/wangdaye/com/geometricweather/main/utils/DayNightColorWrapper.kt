package wangdaye.com.geometricweather.main.utils

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import wangdaye.com.geometricweather.theme.ThemeManager
import java.util.*

class DayNightColorWrapper(
    context: Context,
    @AttrRes val attrId: Int,
    val colorConsumer: (color: Int, animated: Boolean) -> Unit,
) {
    companion object {
        @JvmStatic
        private val viewWrapperMap = WeakHashMap<View, DayNightColorWrapper>()

        @JvmStatic
        fun bind(
            view: View,
            @AttrRes attrId: Int,
            colorConsumer: (color: Int, animated: Boolean) -> Unit,
        ) {
            viewWrapperMap[view] = DayNightColorWrapper(view.context, attrId, colorConsumer)
        }

        @JvmStatic
        fun updateAll() {
            for (entry in viewWrapperMap) {
                entry.value.colorConsumer(
                    entry.value.getColor(entry.key.context),
                    true,
                )
            }
        }
    }

    init {
        colorConsumer(getColor(context), false)
    }

    private fun getColor(context: Context) = ThemeManager
        .getInstance(context)
        .getThemeColor(context, attrId)
}