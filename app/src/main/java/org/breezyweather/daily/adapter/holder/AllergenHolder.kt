package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import org.breezyweather.common.ui.composables.AllergenGrid
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.DailyPollen
import org.breezyweather.databinding.ItemWeatherDailyPollenBinding
import org.breezyweather.theme.compose.BreezyWeatherTheme

class AllergenHolder(
    private val mBinding: ItemWeatherDailyPollenBinding
) : DailyWeatherAdapter.ViewHolder(
    mBinding.root
) {
    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        mBinding.composeView.setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                AllergenGrid(allergen = (model as DailyPollen).allergen)
            }
        }
    }
}