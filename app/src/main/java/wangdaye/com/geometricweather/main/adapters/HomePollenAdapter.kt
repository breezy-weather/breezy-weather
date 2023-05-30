package wangdaye.com.geometricweather.main.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.unit.PollenUnit
import wangdaye.com.geometricweather.common.basic.models.weather.Daily
import wangdaye.com.geometricweather.common.basic.models.weather.Pollen
import wangdaye.com.geometricweather.databinding.ItemPollenDailyBinding
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider

open class HomePollenAdapter @JvmOverloads constructor(
    private val location: Location,
    private val pollenUnit: PollenUnit = PollenUnit.PPCM,
) : RecyclerView.Adapter<HomePollenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePollenViewHolder {
        return HomePollenViewHolder(
            ItemPollenDailyBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: HomePollenViewHolder, position: Int) {
        holder.onBindView(location, location.weather!!.dailyForecast[position], pollenUnit)
    }

    override fun getItemCount(): Int {
        return location.weather?.dailyForecast?.size ?: 0
    }
}

class HomePollenViewHolder internal constructor(
    private val binding: ItemPollenDailyBinding
) : RecyclerView.ViewHolder(
    binding.root
) {
    @SuppressLint("SetTextI18n", "RestrictedApi")
    fun onBindView(location: Location, daily: Daily, unit: PollenUnit) {
        val context = itemView.context
        val pollen = daily.pollen

        binding.title.text = daily.getDate(context.getString(R.string.date_format_widget_long), location.timeZone)
        binding.title.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))

        binding.grassIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.grassLevel)
        )
        binding.grassTitle.text = context.getString(R.string.grass)
        binding.grassTitle.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))
        binding.grassValue.text = (
                unit.getValueText(context, pollen.grassIndex ?: 0)
                        + " - "
                        + pollen.grassDescription
                )
        binding.grassValue.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))

        binding.ragweedIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.ragweedLevel)
        )
        binding.ragweedTitle.text = context.getString(R.string.ragweed)
        binding.ragweedTitle.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))
        binding.ragweedValue.text = (
                unit.getValueText(context, pollen.ragweedIndex ?: 0)
                        + " - "
                        + pollen.ragweedDescription
                )
        binding.ragweedValue.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))

        binding.treeIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.treeLevel)
        )
        binding.treeTitle.text = context.getString(R.string.tree)
        binding.treeTitle.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))
        binding.treeValue.text = (
                unit.getValueText(context, pollen.treeIndex ?: 0)
                        + " - "
                        + pollen.treeDescription
                )
        binding.treeValue.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))

        binding.moldIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.moldLevel)
        )
        binding.moldTitle.text = context.getString(R.string.mold)
        binding.moldTitle.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))
        binding.moldValue.text = (
                unit.getValueText(context, pollen.moldIndex ?: 0)
                        + " - "
                        + pollen.moldDescription
                )
        binding.moldValue.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))

        itemView.contentDescription = (
                binding.title.text.toString()
                        + ", " + context.getString(R.string.grass)
                        + " : " + unit.getValueVoice(context, pollen.grassIndex ?: 0)
                        + " - " + pollen.grassDescription //
                        + ", " + context.getString(R.string.ragweed)
                        + " : " + unit.getValueVoice(context, pollen.ragweedIndex ?: 0)
                        + " - " + pollen.ragweedDescription //
                        + ", " + context.getString(R.string.tree)
                        + " : " + unit.getValueVoice(context, pollen.treeIndex ?: 0)
                        + " - " + pollen.treeDescription //
                        + ", " + context.getString(R.string.mold)
                        + " : " + unit.getValueVoice(context, pollen.moldIndex ?: 0)
                        + " - " + pollen.moldDescription
                )

        itemView.setOnClickListener { }
    }
}