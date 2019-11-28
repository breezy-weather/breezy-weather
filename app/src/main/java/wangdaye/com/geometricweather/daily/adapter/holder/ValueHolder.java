package wangdaye.com.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.Value;

public class ValueHolder extends DailyWeatherAdapter.ViewHolder {

    private TextView title;
    private TextView value;

    public ValueHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_value, parent, false));
        title = itemView.findViewById(R.id.item_weather_daily_value_title);
        value = itemView.findViewById(R.id.item_weather_daily_value_value);
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Value v = (Value) model;
        title.setText(v.getTitle());
        value.setText(v.getValue());
    }
}
