package wangdaye.com.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Astro;
import wangdaye.com.geometricweather.basic.model.weather.MoonPhase;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.DailyAstro;
import wangdaye.com.geometricweather.ui.widget.astro.MoonPhaseView;

public class AstroHolder extends DailyWeatherAdapter.ViewHolder {

    private LinearLayout sun;
    private TextView sunText;

    private LinearLayout moon;
    private TextView moonText;

    private LinearLayout moonPhase;
    private MoonPhaseView moonPhaseIcon;
    private TextView moonPhaseText;

    public AstroHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_astro, parent, false));
        sun = itemView.findViewById(R.id.item_weather_daily_astro_sun);
        sunText = itemView.findViewById(R.id.item_weather_daily_astro_sunText);
        moon = itemView.findViewById(R.id.item_weather_daily_astro_moon);
        moonText = itemView.findViewById(R.id.item_weather_daily_astro_moonText);
        moonPhase = itemView.findViewById(R.id.item_weather_daily_astro_moonPhase);
        moonPhaseIcon = itemView.findViewById(R.id.item_weather_daily_astro_moonPhaseIcon);
        moonPhaseText = itemView.findViewById(R.id.item_weather_daily_astro_moonPhaseText);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Context context = itemView.getContext();
        Astro s = ((DailyAstro) model).getSun();
        Astro m = ((DailyAstro) model).getMoon();
        MoonPhase p = ((DailyAstro) model).getMoonPhase();

        if (s.isValid()) {
            sun.setVisibility(View.VISIBLE);
            sunText.setText(s.getRiseTime(context) + "↑ / " + s.getSetTime(context) + "↓");
        } else {
            sun.setVisibility(View.GONE);
        }
        if (m.isValid()) {
            moon.setVisibility(View.VISIBLE);
            moonText.setText(m.getRiseTime(context) + "↑ / " + m.getSetTime(context) + "↓");
        } else {
            moon.setVisibility(View.GONE);
        }
        if (p.isValid()) {
            moonPhase.setVisibility(View.VISIBLE);
            moonPhaseIcon.setSurfaceAngle(p.getAngle());
            moonPhaseIcon.setColor(
                    ContextCompat.getColor(context, R.color.colorTextContent_dark),
                    ContextCompat.getColor(context, R.color.colorTextContent_light),
                    ContextCompat.getColor(context, R.color.colorTextContent)
            );
            moonPhaseText.setText(p.getMoonPhase(context));
        } else {
            moonPhase.setVisibility(View.GONE);
        }
    }
}