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
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetMaterialYouCurrentProvider
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory

class MaterialYouCurrentWidgetIMP: AbstractRemoteViewsPresenter() {

    companion object {

        fun isEnabled(context: Context): Boolean {
            return AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WidgetMaterialYouCurrentProvider::class.java)
            ).isNotEmpty()
        }

        fun updateWidgetView(context: Context, location: Location?) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, WidgetMaterialYouCurrentProvider::class.java),
                buildRemoteViews(context, location, R.layout.widget_material_you_current)
            )
        }
    }
}

private fun buildRemoteViews(
    context: Context,
    location: Location?,
    @LayoutRes layoutId: Int,
): RemoteViews {

    val views = RemoteViews(context.packageName, layoutId)

    val weather = location?.weather ?: return views

    val provider = ResourcesProviderFactory.newInstance

    val settings = SettingsManager.getInstance(context)
    val temperatureUnit = settings.temperatureUnit

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
    } ?: views.setViewVisibility(R.id.widget_material_you_current_currentIcon, View.INVISIBLE)

    views.setTextViewText(
        R.id.widget_material_you_current_currentTemperature,
        weather.current?.temperature?.temperature?.let {
            temperatureUnit.getShortValueText(context, it)
        }
    )

    // pending intent.
    views.setOnClickPendingIntent(
        android.R.id.background,
        AbstractRemoteViewsPresenter.getWeatherPendingIntent(
            context,
            location,
            Widgets.MATERIAL_YOU_CURRENT_PENDING_INTENT_CODE_WEATHER
        )
    )

    return views
}