package wangdaye.com.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.Overview;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.AnimatableIconView;

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
                + " " + overview.getHalfDay().getTemperature().getTemperature(unit));
    }
}
