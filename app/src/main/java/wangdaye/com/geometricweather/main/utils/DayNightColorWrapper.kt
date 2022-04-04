package wangdaye.com.geometricweather.main.utils

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import wangdaye.com.geometricweather.theme.ThemeManager
import java.util.*

class DayNightColorWrapper(
    context: Context,
    @AttrRes val attrIds: Array<Int>,
    val colorConsumer: (colors: Array<Int>, animated: Boolean) -> Unit,
) {
    companion object {
        @JvmStatic
        private val viewWrapperMap = WeakHashMap<View, DayNightColorWrapper>()

        @JvmStatic
        fun bind(
            view: View,
            @AttrRes attrId: Int,
            colorConsumer: (color: Int, animated: Boolean) -> Unit,
        ) = bind(
            view = view,
            attrIds = arrayOf(attrId),
            colorConsumer = { colors, animated -> colorConsumer(colors[0], animated) },
        )

        @JvmStatic
        fun bind(
            view: View,
            @AttrRes attrIds: Array<Int>,
            colorConsumer: (colors: Array<Int>, animated: Boolean) -> Unit,
        ) {
            viewWrapperMap[view] = DayNightColorWrapper(view.context, attrIds, colorConsumer)
        }

        @JvmStatic
        fun updateAll() {
            for (entry in viewWrapperMap) {
                entry.value.update(
                    context = entry.key.context,
                    animated = true,
                )
            }
        }
    }

    init {
        update(context = context, animated = false)
    }

    private fun update(context: Context, animated: Boolean) {
        colorConsumer(
            attrIds.map { getColor(context, it) }.toTypedArray(),
            animated
        )
    }

    private fun getColor(context: Context, @AttrRes id: Int) = ThemeManager
        .getInstance(context)
        .getThemeColor(context, id)
}