package com.mbestavros.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.model.option.unit.PollenUnit;
import com.mbestavros.geometricweather.basic.model.weather.Pollen;
import com.mbestavros.geometricweather.daily.adapter.DailyWeatherAdapter;
import com.mbestavros.geometricweather.daily.adapter.model.DailyPollen;
import com.mbestavros.geometricweather.databinding.ItemWeatherDailyPollenBinding;

public class PollenHolder extends DailyWeatherAdapter.ViewHolder {

    private ItemWeatherDailyPollenBinding binding;
    private PollenUnit unit;

    public PollenHolder(ItemWeatherDailyPollenBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.unit = PollenUnit.PPCM;
    }

    @SuppressLint({"SetTextI18n", "RestrictedApi"})
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Context context = itemView.getContext();
        Pollen pollen = ((DailyPollen) model).getPollen();

        binding.grassIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getGrassLevel())
        ));
        binding.grassTitle.setText(context.getString(R.string.grass));
        binding.grassValue.setText(unit.getPollenText(context, pollen.getGrassIndex())
                + " - " + pollen.getGrassDescription());

        binding.ragweedIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getRagweedLevel())
        ));
        binding.ragweedTitle.setText(context.getString(R.string.ragweed));
        binding.ragweedValue.setText(unit.getPollenText(context, pollen.getRagweedIndex())
                + " - " + pollen.getRagweedDescription());

        binding.treeIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getTreeLevel())
        ));
        binding.treeTitle.setText(context.getString(R.string.tree));
        binding.treeValue.setText(unit.getPollenText(context, pollen.getTreeIndex())
                + " - " + pollen.getTreeDescription());

        binding.moldIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getMoldLevel())
        ));
        binding.moldTitle.setText(context.getString(R.string.mold));
        binding.moldValue.setText(unit.getPollenText(context, pollen.getMoldIndex())
                + " - " + pollen.getMoldDescription());
    }
}