package wangdaye.com.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.weather.Wind;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.DailyWind;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class WindHolder extends DailyWeatherAdapter.ViewHolder {

    private AppCompatImageView icon;

    private TextView directionText;

    private LinearLayout speed;
    private TextView speedText;

    private TextView gageText;

    private SpeedUnit unit;

    public WindHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_wind, parent, false));

        icon = itemView.findViewById(R.id.item_weather_daily_wind_arrow);
        directionText = itemView.findViewById(R.id.item_weather_daily_wind_directionValue);
        speed = itemView.findViewById(R.id.item_weather_daily_wind_speed);
        speedText = itemView.findViewById(R.id.item_weather_daily_wind_speedValue);
        gageText = itemView.findViewById(R.id.item_weather_daily_wind_levelValue);

        unit = SettingsOptionManager.getInstance(parent.getContext()).getSpeedUnit();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Wind wind = ((DailyWind) model).getWind();

        icon.setSupportImageTintList(ColorStateList.valueOf(wind.getWindColor(itemView.getContext())));
        icon.setRotation(wind.getDegree().getDegree() + 180);

        if (wind.getDegree().isNoDirection() || wind.getDegree().getDegree() % 45 == 0) {
            directionText.setText(wind.getDirection());
        } else {
            directionText.setText(wind.getDirection()
                    + " (" + (int) (wind.getDegree().getDegree() % 360) + "°)");
        }

        if (wind.getSpeed() != null && wind.getSpeed() > 0) {
            speed.setVisibility(View.VISIBLE);
            speedText.setText(unit.getSpeedText(wind.getSpeed()));
        } else {
            speed.setVisibility(View.GONE);
        }

        gageText.setText(wind.getLevel());
    }
}
