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
import wangdaye.com.geometricweather.main.utils.MainThemeContextProvider
import wangdaye.com.geometricweather.theme.ThemeManager

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

        val tm = ThemeManager.getInstance(context)
        val themeCtx = MainThemeContextProvider.getContext(location)

        binding.title.text = daily.getDate(context.getString(R.string.date_format_widget_long))
        binding.title.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorTitleText))

        binding.grassIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.grassLevel)
        )
        binding.grassTitle.text = context.getString(R.string.grass)
        binding.grassTitle.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorBodyText))
        binding.grassValue.text = (
                unit.getPollenText(context, pollen.grassIndex)
                        + " - "
                        + pollen.grassDescription
                )
        binding.grassValue.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorCaptionText))

        binding.ragweedIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.ragweedLevel)
        )
        binding.ragweedTitle.text = context.getString(R.string.ragweed)
        binding.ragweedTitle.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorBodyText))
        binding.ragweedValue.text = (
                unit.getPollenText(context, pollen.ragweedIndex)
                        + " - "
                        + pollen.ragweedDescription
                )
        binding.ragweedValue.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorCaptionText))

        binding.treeIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.treeLevel)
        )
        binding.treeTitle.text = context.getString(R.string.tree)
        binding.treeTitle.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorBodyText))
        binding.treeValue.text = (
                unit.getPollenText(context, pollen.treeIndex)
                        + " - "
                        + pollen.treeDescription
                )
        binding.treeValue.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorCaptionText))

        binding.moldIcon.supportImageTintList = ColorStateList.valueOf(
            Pollen.getPollenColor(itemView.context, pollen.moldLevel)
        )
        binding.moldTitle.text = context.getString(R.string.mold)
        binding.moldTitle.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorBodyText))
        binding.moldValue.text = (
                unit.getPollenText(context, pollen.moldIndex)
                        + " - "
                        + pollen.moldDescription
                )
        binding.moldValue.setTextColor(tm.getThemeColor(themeCtx, R.attr.colorCaptionText))

        itemView.contentDescription = (
                binding.title.text.toString()
                        + ", " + context.getString(R.string.grass)
                        + " : " + unit.getPollenVoice(context, pollen.grassIndex)
                        + " - " + pollen.grassDescription //
                        + ", " + context.getString(R.string.ragweed)
                        + " : " + unit.getPollenVoice(context, pollen.ragweedIndex)
                        + " - " + pollen.ragweedDescription //
                        + ", " + context.getString(R.string.tree)
                        + " : " + unit.getPollenVoice(context, pollen.treeIndex)
                        + " - " + pollen.treeDescription //
                        + ", " + context.getString(R.string.mold)
                        + " : " + unit.getPollenVoice(context, pollen.moldIndex)
                        + " - " + pollen.moldDescription
                )

        itemView.setOnClickListener { }
    }
}