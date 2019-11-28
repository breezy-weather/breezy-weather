package wangdaye.com.geometricweather.daily.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;

public class MarginHolder extends DailyWeatherAdapter.ViewHolder {

    public MarginHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_margin, parent, false));
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        // do nothing.
    }
}
