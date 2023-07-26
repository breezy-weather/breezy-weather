package org.breezyweather.main.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.AllergenUnit
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.ui.composables.AllergenGrid
import org.breezyweather.databinding.ItemPollenDailyBinding
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.compose.BreezyWeatherTheme

open class HomePollenAdapter @JvmOverloads constructor(
    private val location: Location,
    private val allergenUnit: AllergenUnit = AllergenUnit.PPCM,
) : RecyclerView.Adapter<HomePollenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePollenViewHolder {
        return HomePollenViewHolder(
            ItemPollenDailyBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: HomePollenViewHolder, position: Int) {
        holder.onBindView(location, location.weather!!.dailyForecast[position], allergenUnit)
    }

    override fun getItemCount() = location.weather?.dailyForecast?.filter { it.allergen?.isValid == true }?.size ?: 0
}

class HomePollenViewHolder internal constructor(
    private val binding: ItemPollenDailyBinding
) : RecyclerView.ViewHolder(
    binding.root
) {
    @SuppressLint("SetTextI18n", "RestrictedApi")
    fun onBindView(location: Location, daily: Daily, unit: AllergenUnit) {
        val context = itemView.context

        binding.title.text = daily.date.getFormattedDate(location.timeZone, context.getString(R.string.date_format_widget_long))
        binding.title.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))

        daily.allergen?.let {
            binding.composeView.setContent {
                BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                    AllergenGrid(allergen = it)
                }
            }
        }

        itemView.setOnClickListener { }
    }
}