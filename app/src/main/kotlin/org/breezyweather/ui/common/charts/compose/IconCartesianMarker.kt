/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.common.charts.compose

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import kotlin.math.roundToInt

/**
 * A persistent [CartesianMarker] that draws a small bitmap above the chart, at the pixel
 * position of each of its targets' x values.
 *
 * ### Why this exists
 * In Vico 2.x, top/bottom axis labels could contain images by using `SpannableString` with an
 * `ImageSpan`, since axis labels were ultimately drawn with `android.text` APIs shared with
 * Views. In Vico 3.x, `compose` is a genuine Compose Multiplatform module: drawing is done
 * exclusively with Compose's own `Canvas`/`Paint`, so `android.text` spans have no effect
 * anymore. `CartesianMarker` is a small, focused interface (`drawOverLayers`/`drawUnderLayers`
 * plus layer-margin reservation) that draws directly on that `Canvas`, so it can draw arbitrary
 * bitmaps without any text-span workaround.
 *
 * ### Usage
 * Register one instance per x value you want an icon for, via [rememberCartesianChart]'s
 * `persistentMarkers` parameter:
 * ```
 * val iconMarker = remember(...) { IconCartesianMarker(itemPlacer = ..., iconProvider = ...) }
 * rememberCartesianChart(
 *     // ...
 *     persistentMarkers = { _ -> xValues.forEach { iconMarker at it } },
 * )
 * ```
 * Registering an x value doesn't guarantee it gets an icon: [itemPlacer] decides which of the
 * registered values actually fit, exactly the way it would decide which labels to draw if it
 * were used on a [HorizontalAxis] instead. Reusing the same [HorizontalAxis.ItemPlacer] a chart
 * would otherwise use for its top/bottom axis keeps icon spacing consistent with that axis'
 * label spacing.
 *
 * @param itemPlacer selects, out of all the x values this marker is registered at, which ones
 *   actually get an icon drawn, based on the available width.
 * @param iconSize the width and height at which icons are drawn.
 * @param verticalPadding extra vertical spacing reserved between the chart's data area and the
 *   icon.
 * @param tint if non-null, tints icons with this color. Use this for monochrome icons, such as
 *   arrows. Leave null for full-color icons, such as weather condition icons.
 * @param iconProvider returns the icon to draw for a given x value, or null to draw nothing for
 *   it (for example, if there's no data for that value).
 */
class IconCartesianMarker(
    private val itemPlacer: HorizontalAxis.ItemPlacer,
    private val iconSize: Dp = DEFAULT_ICON_SIZE,
    private val verticalPadding: Dp = 5.dp,
    private val tint: Color? = null,
    private val iconProvider: (x: Double) -> ImageBitmap?,
) : CartesianMarker {

    private val paint = Paint().apply {
        tint?.let { colorFilter = ColorFilter.tint(it, BlendMode.SrcAtop) }
    }

    override fun drawOverLayers(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ) {
        with(context) {
            val iconSizePx = iconSize.pixels
            // Reuses the same selection algorithm an axis would use to decide which labels fit,
            // so icons are skipped the same way labels used to be when there isn't room for all
            // of them (e.g. on a narrow screen with many hourly values).
            val selectedValues = itemPlacer.getLabelValues(
                context = context,
                visibleXRange = ranges.minX..ranges.maxX,
                fullXRange = ranges.minX..ranges.maxX,
                maxLabelWidth = iconSizePx
            )
            val iconSizePxInt = iconSizePx.roundToInt()
            val top = (layerBounds.top - iconSizePx - verticalPadding.pixels).roundToInt()

            targets.forEach { target ->
                if (selectedValues.none { it == target.x }) return@forEach
                val bitmap = iconProvider(target.x) ?: return@forEach
                canvas.drawImageRect(
                    image = bitmap,
                    dstOffset = IntOffset((target.canvasX - iconSizePx / 2f).roundToInt(), top),
                    dstSize = IntSize(iconSizePxInt, iconSizePxInt),
                    paint = paint
                )
            }
        }
    }

    override fun updateLayerMargins(
        context: CartesianMeasuringContext,
        layerMargins: CartesianLayerMargins,
        layerDimensions: CartesianLayerDimensions,
        model: CartesianChartModel,
    ) {
        with(context) {
            layerMargins.ensureValuesAtLeast(top = iconSize.pixels + verticalPadding.pixels)
        }
    }

    companion object {
        /**
         * The default [iconSize]. Icon providers should rasterize their source drawable at this
         * size (in px, via [androidx.compose.ui.unit.Density]) rather than at the drawable's own
         * intrinsic size. Otherwise, the icon gets drawn once at its native resolution and then
         * bitmap-scaled to fit, which softens thin strokes into a faded, greyish blur.
         */
        val DEFAULT_ICON_SIZE = 18.dp
    }
}
