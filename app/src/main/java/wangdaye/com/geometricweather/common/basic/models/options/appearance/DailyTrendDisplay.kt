package wangdaye.com.geometricweather.common.basic.models.options.appearance

import android.content.Context
import android.text.TextUtils
import androidx.annotation.StringRes
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.BaseEnum

enum class DailyTrendDisplay(
    override val id: String,
    @StringRes val nameId: Int
): BaseEnum {

    TAG_TEMPERATURE("temperature", R.string.temperature), 
    TAG_AIR_QUALITY("air_quality", R.string.air_quality),
    TAG_WIND("wind", R.string.wind), 
    TAG_UV_INDEX("uv_index", R.string.uv_index), 
    TAG_PRECIPITATION("precipitation", R.string.precipitation);

    companion object {

        @JvmStatic
        fun toDailyTrendDisplayList(
            value: String
        ) = if (TextUtils.isEmpty(value)) {
            ArrayList()
        } else try {
            val cards = value.split("&").toTypedArray()
            val list: MutableList<DailyTrendDisplay> = ArrayList()
            for (card in cards) {
                when (card) {
                    "temperature" -> list.add(TAG_TEMPERATURE)
                    "air_quality" -> list.add(TAG_AIR_QUALITY)
                    "wind" -> list.add(TAG_WIND)
                    "uv_index" -> list.add(TAG_UV_INDEX)
                    "precipitation" -> list.add(TAG_PRECIPITATION)
                }
            }
            list
        } catch (e: Exception) {
            ArrayList()
        }

        @JvmStatic
        fun toValue(list: List<DailyTrendDisplay>): String {
            val builder = StringBuilder()
            for (v in list) {
                builder.append("&").append(v.id)
            }
            if (builder.isNotEmpty() && builder[0] == '&') {
                builder.deleteCharAt(0)
            }
            return builder.toString()
        }

        @JvmStatic
        fun getSummary(context: Context, list: List<DailyTrendDisplay>): String {
            val builder = StringBuilder()
            for (item in list) {
                builder.append(",").append(item.getName(context))
            }
            if (builder.isNotEmpty() && builder[0] == ',') {
                builder.deleteCharAt(0)
            }
            return builder.toString().replace(",", ", ")
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)
}