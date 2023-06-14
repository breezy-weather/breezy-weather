package org.breezyweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.ui.widgets.AnimatableIconView;
import org.breezyweather.theme.resource.ResourcesProviderFactory;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;
import org.breezyweather.daily.adapter.DailyWeatherAdapter;
import org.breezyweather.daily.adapter.model.Overview;
import org.breezyweather.settings.SettingsManager;

public class OverviewHolder extends DailyWeatherAdapter.ViewHolder {

    private AnimatableIconView mIcon;
    private final TextView mTitle;

    private final ResourceProvider mProvider;
    private final TemperatureUnit mTemperatureUnit;

    public OverviewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_overview, parent, false));
        itemView.setOnClickListener(v -> mIcon.startAnimators());

        mIcon = itemView.findViewById(R.id.item_weather_daily_overview_icon);
        mTitle = itemView.findViewById(R.id.item_weather_daily_overview_text);

        mProvider = ResourcesProviderFactory.getNewInstance();
        mTemperatureUnit = SettingsManager.getInstance(parent.getContext()).getTemperatureUnit();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Overview overview = (Overview) model;
        if (overview.getHalfDay().getWeatherCode() != null) {
            mIcon.setAnimatableIcon(
                    mProvider.getWeatherIcons(overview.getHalfDay().getWeatherCode(), overview.isDaytime()),
                    mProvider.getWeatherAnimators(overview.getHalfDay().getWeatherCode(), overview.isDaytime())
            );
        }
        StringBuilder builder = new StringBuilder();
        if (overview.getHalfDay().getWeatherText() != null) {
            builder.append(overview.getHalfDay().getWeatherText());
        }
        if (overview.getHalfDay().getTemperature() != null && !TextUtils.isEmpty(overview.getHalfDay().getTemperature().getTemperature(mTitle.getContext(), mTemperatureUnit))) {
            if (!TextUtils.isEmpty(builder.toString())) {
                builder.append(", ");
            }
            builder.append(overview.getHalfDay().getTemperature().getTemperature(mTitle.getContext(), mTemperatureUnit));
        }
        if (!TextUtils.isEmpty(builder.toString())) {
            mTitle.setText(builder.toString());
        }
    }
}
