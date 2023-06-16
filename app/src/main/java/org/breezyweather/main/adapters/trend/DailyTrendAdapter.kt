package org.breezyweather.main.adapters.trend

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.main.adapters.trend.daily.*
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourcesProviderFactory

@SuppressLint("NotifyDataSetChanged")
class DailyTrendAdapter(
    private val activity: GeoActivity,
    private val host: TrendRecyclerView,
) : RecyclerView.Adapter<AbsDailyTrendAdapter.ViewHolder>() {

    var adapters: Array<AbsDailyTrendAdapter> = emptyArray()
        private set

    var selectedIndex = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var selectedIndexCache = -1

    fun bindData(location: Location) {
        val provider = ResourcesProviderFactory.getNewInstance()

        adapters = arrayOf(
            DailyTemperatureAdapter(
                activity,
                location,
                provider,
                SettingsManager.getInstance(activity).temperatureUnit
            ),
            DailyAirQualityAdapter(
                activity,
                location
            ),
            DailyWindAdapter(
                activity,
                location,
                SettingsManager.getInstance(activity).speedUnit
            ),
            DailyUVAdapter(activity, location),
            DailyPrecipitationAdapter(
                activity,
                location,
                provider,
                SettingsManager.getInstance(activity).precipitationUnit
            ),
        ).filter {
            it.isValid(location)
        }.toTypedArray()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsDailyTrendAdapter.ViewHolder {
        return adapters[selectedIndex].onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        adapters[selectedIndex].onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return adapters.getOrNull(selectedIndex)?.itemCount ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        if (selectedIndexCache != selectedIndex) {
            selectedIndexCache = selectedIndex
            adapters[selectedIndex].bindBackgroundForHost(host)
        }
        return selectedIndex
    }
}