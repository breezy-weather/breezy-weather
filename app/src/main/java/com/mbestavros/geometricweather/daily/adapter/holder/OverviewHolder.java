package com.mbestavros.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.model.option.unit.TemperatureUnit;
import com.mbestavros.geometricweather.daily.adapter.DailyWeatherAdapter;
import com.mbestavros.geometricweather.daily.adapter.model.Overview;
import com.mbestavros.geometricweather.resource.provider.ResourceProvider;
import com.mbestavros.geometricweather.resource.provider.ResourcesProviderFactory;
import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.ui.widget.AnimatableIconView;

public class OverviewHolder extends DailyWeatherAdapter.ViewHolder {

    private AnimatableIconView icon;
    private TextView title;

    private ResourceProvider provider;
    private TemperatureUnit unit;

    public OverviewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_overview, parent, false));
        itemView.setOnClickListener(v -> icon.startAnimators());

        icon = itemView.findViewById(R.id.item_weather_daily_overview_icon);
        title = itemView.findViewById(R.id.item_weather_daily_overview_text);

        provider = ResourcesProviderFactory.getNewInstance();
        unit = SettingsOptionManager.getInstance(parent.getContext()).getTemperatureUnit();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Overview overview = (Overview) model;
        icon.setAnimatableIcon(
                provider.getWeatherIcons(overview.getHalfDay().getWeatherCode(), overview.isDaytime()),
                provider.getWeatherAnimators(overview.getHalfDay().getWeatherCode(), overview.isDaytime())
        );
        title.setText(overview.getHalfDay().getWeatherText()
                + " " + overview.getHalfDay().getTemperature().getTemperature(title.getContext(), unit));
    }
}
