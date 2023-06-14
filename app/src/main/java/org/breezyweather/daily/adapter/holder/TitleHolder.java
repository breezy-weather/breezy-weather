package org.breezyweather.daily.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.breezyweather.R;
import org.breezyweather.daily.adapter.DailyWeatherAdapter;
import org.breezyweather.daily.adapter.model.Title;

public class TitleHolder extends DailyWeatherAdapter.ViewHolder {

    private final ImageView mIcon;
    private final TextView mTitle;

    public TitleHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_title, parent, false));

        mIcon = itemView.findViewById(R.id.item_weather_daily_title_icon);
        mTitle = itemView.findViewById(R.id.item_weather_daily_title_title);
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Title t = (Title) model;

        if (t.getResId() != null) {
            mIcon.setVisibility(View.VISIBLE);
            mIcon.setImageResource(t.getResId());
        } else {
            mIcon.setVisibility(View.GONE);
        }

        mTitle.setText(t.getTitle());
    }
}
