package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.PollenUnit
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.DailyPollen
import org.breezyweather.databinding.ItemWeatherDailyPollenBinding

class PollenHolder(
    private val mBinding: ItemWeatherDailyPollenBinding
) : DailyWeatherAdapter.ViewHolder(
    mBinding.root
) {
    private val mPollenUnit: PollenUnit = PollenUnit.PPCM

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val context = itemView.context
        val pollen = (model as DailyPollen).pollen
        mBinding.grassIcon.supportImageTintList = ColorStateList.valueOf(
            pollen.getGrassColor(itemView.context)
        )
        mBinding.grassTitle.text = context.getString(R.string.allergen_grass)
        mBinding.grassValue.text = mPollenUnit.getValueText(
            context, pollen.grassIndex ?: 0
        ) + " - " + pollen.grassDescription
        mBinding.ragweedIcon.supportImageTintList = ColorStateList.valueOf(
            pollen.getRagweedColor(itemView.context)
        )
        mBinding.ragweedTitle.text = context.getString(R.string.allergen_ragweed)
        mBinding.ragweedValue.text = mPollenUnit.getValueText(
            context, pollen.ragweedIndex ?: 0
        ) + " - " + pollen.ragweedDescription
        mBinding.treeIcon.supportImageTintList = ColorStateList.valueOf(
            pollen.getTreeColor(itemView.context)
        )
        mBinding.treeTitle.text = context.getString(R.string.allergen_tree)
        mBinding.treeValue.text = mPollenUnit.getValueText(
            context, pollen.treeIndex ?: 0
        ) + " - " + pollen.treeDescription
        mBinding.moldIcon.supportImageTintList = ColorStateList.valueOf(
            pollen.getMoldColor(itemView.context)
        )
        mBinding.moldTitle.text = context.getString(R.string.allergen_mold)
        mBinding.moldValue.text = mPollenUnit.getValueText(
            context, pollen.moldIndex ?: 0
        ) + " - " + pollen.moldDescription
    }
}