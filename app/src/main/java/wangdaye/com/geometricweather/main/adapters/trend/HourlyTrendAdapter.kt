package wangdaye.com.geometricweather.main.adapters.trend

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView
import wangdaye.com.geometricweather.main.adapters.trend.hourly.*
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory

@SuppressLint("NotifyDataSetChanged")
class HourlyTrendAdapter(
    private val activity: GeoActivity,
    private val host: TrendRecyclerView,
) : RecyclerView.Adapter<AbsHourlyTrendAdapter.ViewHolder>() {

    var adapters: Array<AbsHourlyTrendAdapter> = emptyArray()
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
            HourlyTemperatureAdapter(
                activity,
                location,
                provider,
                SettingsManager.getInstance(activity).temperatureUnit
            ),
            HourlyWindAdapter(
                activity,
                location,
                SettingsManager.getInstance(activity).speedUnit
            ),
            HourlyUVAdapter(activity, location),
            HourlyPrecipitationAdapter(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsHourlyTrendAdapter.ViewHolder {
        return adapters[selectedIndex].onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
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