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

package org.breezyweather.remoteviews.common

import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import androidx.core.os.BundleCompat
import kotlin.math.roundToInt

object WidgetSizeUtils {

    fun initializeRemoteViews(
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        getRemoteViews: (widgetSize: WidgetSize) -> RemoteViews,
    ): RemoteViews {
        val appWidgetOptions = appWidgetManager.getAppWidgetOptions(widgetId)
        val parcelableArrayList: ArrayList<*>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BundleCompat.getParcelableArrayList(
                appWidgetOptions,
                AppWidgetManager.OPTION_APPWIDGET_SIZES,
                SizeF::class.java
            )
        } else {
            null
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            parcelableArrayList == null ||
            parcelableArrayList.isEmpty()
        ) {
            return RemoteViews(
                getRemoteViews(getWidgetSize(appWidgetOptions, portrait = false)),
                getRemoteViews(getWidgetSize(appWidgetOptions, portrait = true))
            )
        }
        val linkedHashMap = LinkedHashMap<SizeF, RemoteViews>()
        for (obj in parcelableArrayList) {
            val sizeF = obj as SizeF
            linkedHashMap[sizeF] = getRemoteViews(
                WidgetSize(sizeF.width.roundToInt().toFloat(), sizeF.height.roundToInt().toFloat())
            )
        }
        return RemoteViews(linkedHashMap)
    }

    private fun getWidgetSize(bundle: Bundle, portrait: Boolean): WidgetSize {
        val pair = if (portrait) {
            Pair(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        } else {
            Pair(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        }
        return WidgetSize(bundle.getInt(pair.first).toFloat(), bundle.getInt(pair.second).toFloat())
    }
}
