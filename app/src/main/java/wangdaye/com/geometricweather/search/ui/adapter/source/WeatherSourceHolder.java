package wangdaye.com.geometricweather.search.ui.adapter.source;

import android.content.res.ColorStateList;

import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.databinding.ItemWeatherSourceBinding;

class WeatherSourceHolder extends RecyclerView.ViewHolder {

    private final ItemWeatherSourceBinding mBinding;

    WeatherSourceHolder(ItemWeatherSourceBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    void onBind(WeatherSourceModel model) {
        mBinding.checkbox.setChecked(model.isEnabled());
        mBinding.title.setText(model.getSource().getSourceName(itemView.getContext()));

        int color = model.getSource().getSourceColor();
        CompoundButtonCompat.setButtonTintList(mBinding.checkbox, ColorStateList.valueOf(color));

        mBinding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.setEnabled(isChecked);
            mBinding.checkbox.setChecked(model.isEnabled());
        });
        mBinding.container.setOnClickListener(v -> {
            model.setEnabled(!model.isEnabled());
            mBinding.checkbox.setChecked(model.isEnabled());
        });
    }
}
