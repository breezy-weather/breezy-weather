package wangdaye.com.geometricweather.common.basic.models.weather

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import wangdaye.com.geometricweather.R
import java.io.Serializable

/**
 * Pollen.
 */
class Pollen(
    val grassIndex: Int? = null,
    val grassLevel: Int? = null,
    val grassDescription: String? = null,

    val moldIndex: Int? = null,
    val moldLevel: Int? = null,
    val moldDescription: String? = null,

    val ragweedIndex: Int? = null,
    val ragweedLevel: Int? = null,
    val ragweedDescription: String? = null,

    val treeIndex: Int? = null,
    val treeLevel: Int? = null,
    val treeDescription: String? = null
) : Serializable {

    @ColorInt
    private fun getPollenColor(context: Context, level: Int?): Int {
        return if (level == null) {
            Color.TRANSPARENT
        } else when (level) {
            in 0..1 -> ContextCompat.getColor(context, R.color.colorLevel_1)
            in 1..2 -> ContextCompat.getColor(context, R.color.colorLevel_2)
            in 2..3 -> ContextCompat.getColor(context, R.color.colorLevel_3)
            in 3..4 -> ContextCompat.getColor(context, R.color.colorLevel_4)
            in 4..5 -> ContextCompat.getColor(context, R.color.colorLevel_5)
            in 5..Int.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_6)
            else -> Color.TRANSPARENT
        }
    }

    @ColorInt
    fun getGrassColor(context: Context) = getPollenColor(context, grassLevel)
    @ColorInt
    fun getMoldColor(context: Context) = getPollenColor(context, moldLevel)
    @ColorInt
    fun getRagweedColor(context: Context) = getPollenColor(context, ragweedLevel)
    @ColorInt
    fun getTreeColor(context: Context) = getPollenColor(context, treeLevel)

    val isValid: Boolean
        get() = grassIndex != null && grassIndex > 0 && grassLevel != null
                || moldIndex != null && moldIndex > 0 && moldLevel != null
                || ragweedIndex != null && ragweedIndex > 0 && ragweedLevel != null
                || treeIndex != null && treeIndex > 0 && treeLevel != null
}
