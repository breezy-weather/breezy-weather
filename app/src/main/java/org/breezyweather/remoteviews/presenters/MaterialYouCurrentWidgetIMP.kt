/**
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

package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetMaterialYouCurrentProvider
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.options.NotificationTextColor
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.remoteviews.common.WidgetSize
import org.breezyweather.remoteviews.common.WidgetSizeUtils
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.unit.formatting.UnitWidth
import kotlin.math.min
import kotlin.math.roundToInt

class MaterialYouCurrentWidgetIMP : AbstractRemoteViewsPresenter() {

    companion object {

        fun isEnabled(context: Context): Boolean {
            return AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WidgetMaterialYouCurrentProvider::class.java)
            ).isNotEmpty()
        }

        fun updateWidgetView(context: Context, location: Location?) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val defaultDimension = context.resources.getDimensionPixelSize(
                R.dimen.widget_material_you_default_size
            ).toDouble()
            appWidgetManager.getAppWidgetIds(
                ComponentName(context, WidgetMaterialYouCurrentProvider::class.java)
            ).forEach { widgetId ->
                val views = WidgetSizeUtils.initializeRemoteViews(
                    appWidgetManager,
                    widgetId
                ) { widgetSize: WidgetSize ->
                    val layoutId = // if (!isMini(context, widgetSize)) {
                        R.layout.widget_material_you_current
                    // } else R.layout.widget_material_you_current_mini
                    val remoteViews = RemoteViews(context.packageName, layoutId)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // if (!isMini(context, widgetSize)) {
                        val min = min(
                            min(widgetSize.widthPx.toDouble(), widgetSize.heightPx.toDouble()),
                            defaultDimension
                        ).toFloat()
                        remoteViews.setViewLayoutWidth(
                            R.id.widget_material_you_current,
                            min,
                            TypedValue.COMPLEX_UNIT_PX
                        )
                        remoteViews.setViewLayoutHeight(
                            R.id.widget_material_you_current,
                            min,
                            TypedValue.COMPLEX_UNIT_PX
                        )
                        // }
                    }

                    buildRemoteViews(context, remoteViews, location, widgetSize)
                }

                AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
            }
        }

        private fun isMini(context: Context, widgetSize: WidgetSize): Boolean {
            return min(
                widgetSize.widthPx.toDouble(),
                widgetSize.heightPx.toDouble()
            ) < context.resources.getDimensionPixelSize(
                R.dimen.widget_material_you_minimum_size_for_square
            )
        }

        private fun buildRemoteViews(
            context: Context,
            views: RemoteViews,
            location: Location?,
            widgetSize: WidgetSize,
        ): RemoteViews {
            val weather = location?.weather ?: return views

            val provider = ResourcesProviderFactory.newInstance
            val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)

            // current.
            weather.current?.weatherCode?.let {
                views.setViewVisibility(R.id.widget_material_you_current_currentIcon, View.VISIBLE)
                views.setImageViewUri(
                    R.id.widget_material_you_current_currentIcon,
                    ResourceHelper.getWidgetNotificationIconUri(
                        provider,
                        it,
                        location.isDaylight,
                        false,
                        NotificationTextColor.LIGHT
                    )
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val defaultDimension = context.resources.getDimensionPixelSize(
                        R.dimen.widget_material_you_default_size
                    ).toDouble()
                    var ratio = min(
                        defaultDimension,
                        min(widgetSize.heightPx, widgetSize.widthPx).toDouble()
                    ).div(defaultDimension)
                    val iconDimension = context.resources.getDimensionPixelSize(
                        R.dimen.widget_material_you_current_default_icon_size
                    )
                    val ratioIcon = ((iconDimension * ratio).roundToInt()).toFloat()
                    views.setViewLayoutWidth(
                        R.id.widget_material_you_current_currentIcon,
                        ratioIcon,
                        TypedValue.COMPLEX_UNIT_PX
                    )
                    views.setViewLayoutHeight(
                        R.id.widget_material_you_current_currentIcon,
                        ratioIcon,
                        TypedValue.COMPLEX_UNIT_PX
                    )
                    if (ratio < 1.0) {
                        ratio *= 0.7
                    }
                    val iconMarginDimension = context.resources.getDimensionPixelSize(
                        R.dimen.widget_material_you_current_default_icon_margin
                    ).toDouble()
                    val ratioIconMargin = (iconMarginDimension * ratio).toInt().toFloat()
                    views.setViewLayoutMargin(
                        R.id.widget_material_you_current_currentIcon,
                        RemoteViews.MARGIN_BOTTOM,
                        ratioIconMargin,
                        TypedValue.COMPLEX_UNIT_PX
                    )
                    views.setViewLayoutMargin(
                        R.id.widget_material_you_current_currentIcon,
                        RemoteViews.MARGIN_START,
                        ratioIconMargin,
                        TypedValue.COMPLEX_UNIT_PX
                    )
                }
            } ?: views.setViewVisibility(R.id.widget_material_you_current_currentIcon, View.INVISIBLE)

            val temperatureText = weather.current?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            )
            views.setTextViewText(
                R.id.widget_material_you_current_currentTemperature,
                temperatureText
            )

            views.setTextViewTextSize(
                R.id.widget_material_you_current_currentTemperature,
                TypedValue.COMPLEX_UNIT_DIP,
                if ((temperatureText?.length ?: 0) > 1) 62.0f else 70.0f
            )

            // pending intent.
            views.setOnClickPendingIntent(
                R.id.widget_material_you_current,
                getWeatherPendingIntent(
                    context,
                    location,
                    Widgets.MATERIAL_YOU_CURRENT_PENDING_INTENT_CODE_WEATHER
                )
            )

            return views
        }
    }
}
