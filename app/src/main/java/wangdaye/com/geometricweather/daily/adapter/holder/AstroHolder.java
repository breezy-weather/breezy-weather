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
import wangdaye.com.geometricweather.common.basic.models.weather.Astro;
import wangdaye.com.geometricweather.common.basic.models.weather.MoonPhase;
import wangdaye.com.geometricweather.common.ui.widgets.astro.MoonPhaseView;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.DailyAstro;
import wangdaye.com.geometricweather.theme.ThemeManager;

public class AstroHolder extends DailyWeatherAdapter.ViewHolder {

    private final LinearLayout mSun;
    private final TextView mSunText;

    private final LinearLayout mMoon;
    private final TextView mMoonText;

    private final LinearLayout mMoonPhase;
    private final MoonPhaseView mMoonPhaseIcon;
    private final TextView mMoonPhaseText;

    public AstroHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_astro, parent, false));
        mSun = itemView.findViewById(R.id.item_weather_daily_astro_sun);
        mSunText = itemView.findViewById(R.id.item_weather_daily_astro_sunText);
        mMoon = itemView.findViewById(R.id.item_weather_daily_astro_moon);
        mMoonText = itemView.findViewById(R.id.item_weather_daily_astro_moonText);
        mMoonPhase = itemView.findViewById(R.id.item_weather_daily_astro_moonPhase);
        mMoonPhaseIcon = itemView.findViewById(R.id.item_weather_daily_astro_moonPhaseIcon);
        mMoonPhaseText = itemView.findViewById(R.id.item_weather_daily_astro_moonPhaseText);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Context context = itemView.getContext();
        Astro s = ((DailyAstro) model).getSun();
        Astro m = ((DailyAstro) model).getMoon();
        MoonPhase p = ((DailyAstro) model).getMoonPhase();

        StringBuilder talkBackBuilder = new StringBuilder(context.getString(R.string.sunrise_sunset));

        if (s.isValid()) {
            talkBackBuilder
                    .append(", ")
                    .append(context.getString(R.string.content_des_sunrise).replace("$", s.getRiseTime(context)))
                    .append(", ")
                    .append(context.getString(R.string.content_des_sunset).replace("$", s.getSetTime(context)));

            mSun.setVisibility(View.VISIBLE);
            mSunText.setText(s.getRiseTime(context) + "↑ / " + s.getSetTime(context) + "↓");
        } else {
            mSun.setVisibility(View.GONE);
        }

        if (m.isValid()) {
            talkBackBuilder
                    .append(", ")
                    .append(context.getString(R.string.content_des_moonrise).replace("$", m.getRiseTime(context)))
                    .append(", ")
                    .append(context.getString(R.string.content_des_moonset).replace("$", m.getSetTime(context)));

            mMoon.setVisibility(View.VISIBLE);
            mMoonText.setText(m.getRiseTime(context) + "↑ / " + m.getSetTime(context) + "↓");
        } else {
            mMoon.setVisibility(View.GONE);
        }

        if (p.isValid()) {
            talkBackBuilder.append(", ").append(p.getMoonPhase(context));

            mMoonPhase.setVisibility(View.VISIBLE);
            mMoonPhaseIcon.setSurfaceAngle(p.getAngle());
            mMoonPhaseIcon.setColor(
                    ContextCompat.getColor(context, R.color.colorTextLight2nd),
                    ContextCompat.getColor(context, R.color.colorTextDark2nd),
                    ThemeManager.getInstance(context).getThemeColor(
                            context, R.attr.colorBodyText
                    )
            );
            mMoonPhaseText.setText(p.getMoonPhase(context));
        } else {
            mMoonPhase.setVisibility(View.GONE);
        }

        itemView.setContentDescription(talkBackBuilder.toString());
    }
}