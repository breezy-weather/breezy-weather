package wangdaye.com.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options.unit.PollenUnit;
import wangdaye.com.geometricweather.basic.models.weather.Pollen;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.DailyPollen;
import wangdaye.com.geometricweather.databinding.ItemWeatherDailyPollenBinding;

public class PollenHolder extends DailyWeatherAdapter.ViewHolder {

    private final ItemWeatherDailyPollenBinding mBinding;
    private final PollenUnit mPollenUnit;

    public PollenHolder(ItemWeatherDailyPollenBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
        mPollenUnit = PollenUnit.PPCM;
    }

    @SuppressLint({"SetTextI18n", "RestrictedApi"})
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Context context = itemView.getContext();
        Pollen pollen = ((DailyPollen) model).getPollen();

        mBinding.grassIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getGrassLevel())
        ));
        mBinding.grassTitle.setText(context.getString(R.string.grass));
        mBinding.grassValue.setText(mPollenUnit.getPollenText(context, pollen.getGrassIndex())
                + " - " + pollen.getGrassDescription());

        mBinding.ragweedIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getRagweedLevel())
        ));
        mBinding.ragweedTitle.setText(context.getString(R.string.ragweed));
        mBinding.ragweedValue.setText(mPollenUnit.getPollenText(context, pollen.getRagweedIndex())
                + " - " + pollen.getRagweedDescription());

        mBinding.treeIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getTreeLevel())
        ));
        mBinding.treeTitle.setText(context.getString(R.string.tree));
        mBinding.treeValue.setText(mPollenUnit.getPollenText(context, pollen.getTreeIndex())
                + " - " + pollen.getTreeDescription());

        mBinding.moldIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getMoldLevel())
        ));
        mBinding.moldTitle.setText(context.getString(R.string.mold));
        mBinding.moldValue.setText(mPollenUnit.getPollenText(context, pollen.getMoldIndex())
                + " - " + pollen.getMoldDescription());
    }
}