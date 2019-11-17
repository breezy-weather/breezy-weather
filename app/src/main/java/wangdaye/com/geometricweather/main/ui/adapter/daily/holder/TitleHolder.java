package wangdaye.com.geometricweather.main.ui.adapter.daily.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWeatherAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.daily.model.Title;

public class TitleHolder extends DailyWeatherAdapter.ViewHolder {

    public TitleHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_title, parent, false));
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Title title = (Title) model;
        ((TextView) itemView).setText(title.getTitle());
    }
}
