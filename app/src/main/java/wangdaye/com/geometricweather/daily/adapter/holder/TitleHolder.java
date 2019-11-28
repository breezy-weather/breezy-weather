package wangdaye.com.geometricweather.daily.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.Title;

public class TitleHolder extends DailyWeatherAdapter.ViewHolder {

    private ImageView icon;
    private TextView title;

    public TitleHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_title, parent, false));

        icon = itemView.findViewById(R.id.item_weather_daily_title_icon);
        title = itemView.findViewById(R.id.item_weather_daily_title_title);
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Title t = (Title) model;

        if (t.getResId() != null) {
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(t.getResId());
        } else {
            icon.setVisibility(View.GONE);
        }

        title.setText(t.getTitle());
    }
}
