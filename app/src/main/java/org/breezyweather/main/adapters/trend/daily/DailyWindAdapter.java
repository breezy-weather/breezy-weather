package org.breezyweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.SpeedUnit;
import org.breezyweather.common.basic.models.weather.Daily;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.basic.models.weather.Wind;
import org.breezyweather.common.ui.images.RotateDrawable;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.chart.DoubleHistogramView;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;
import org.breezyweather.settings.SettingsManager;

/**
 * Daily wind adapter.
 **/
public class DailyWindAdapter extends AbsDailyTrendAdapter {

    private final SpeedUnit mSpeedUnit;
    private float mHighestWindSpeed;

    class ViewHolder extends AbsDailyTrendAdapter.ViewHolder {

        private final DoubleHistogramView mDoubleHistogramView;

        ViewHolder(View itemView) {
            super(itemView);

            mDoubleHistogramView = new DoubleHistogramView(itemView.getContext());
            dailyItem.setChartItemView(mDoubleHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_wind));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Daily daily = weather.getDailyForecast().get(position);

            if (daily.getDay() != null && daily.getDay().getWind() != null) {
                talkBackBuilder
                        .append(", ").append(activity.getString(R.string.daytime))
                        .append(" : ").append(daily.getDay().getWind().getWindDescription(activity, mSpeedUnit));

                int daytimeWindColor = daily.getDay().getWind().getWindColor(activity);

                RotateDrawable dayIcon = daily.getDay().getWind().isValidSpeed()
                        ? new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_navigation))
                        : new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium));
                if (daily.getDay().getWind().getDegree() != null && daily.getDay().getWind().getDegree().getDegree() != null) {
                    dayIcon.rotate(daily.getDay().getWind().getDegree().getDegree() + 180);
                }
                dayIcon.setColorFilter(new PorterDuffColorFilter(daytimeWindColor, PorterDuff.Mode.SRC_ATOP));
                dailyItem.setDayIconDrawable(dayIcon);
            }

            if (daily.getNight() != null && daily.getNight().getWind() != null) {
                talkBackBuilder
                        .append(", ").append(activity.getString(R.string.nighttime))
                        .append(" : ").append(daily.getNight().getWind().getWindDescription(activity, mSpeedUnit));
            }

            if (daily.getDay() != null && daily.getDay().getWind() != null
                    && daily.getNight() != null && daily.getNight().getWind() != null) {
                int daytimeWindColor = daily.getDay().getWind().getWindColor(activity);
                int nighttimeWindColor = daily.getNight().getWind().getWindColor(activity);
                Float daytimeWindSpeed = weather.getDailyForecast().get(position).getDay().getWind().getSpeed();
                Float nighttimeWindSpeed = weather.getDailyForecast().get(position).getNight().getWind().getSpeed();
                mDoubleHistogramView.setData(
                        weather.getDailyForecast().get(position).getDay().getWind().getSpeed(),
                        weather.getDailyForecast().get(position).getNight().getWind().getSpeed(),
                        mSpeedUnit.getValueTextWithoutUnit(daytimeWindSpeed == null ? 0 : daytimeWindSpeed),
                        mSpeedUnit.getValueTextWithoutUnit(nighttimeWindSpeed == null ? 0 : nighttimeWindSpeed),
                        mHighestWindSpeed
                );
                mDoubleHistogramView.setLineColors(
                        daytimeWindColor,
                        nighttimeWindColor,
                        MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                );
                mDoubleHistogramView.setTextColors(
                        MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
                );
                mDoubleHistogramView.setHistogramAlphas(1f, 0.5f);
            }

            if (daily.getNight() != null && daily.getNight().getWind() != null) {
                int nighttimeWindColor = daily.getNight().getWind().getWindColor(activity);
                RotateDrawable nightIcon = daily.getNight().getWind().isValidSpeed()
                        ? new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_navigation))
                        : new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium));
                if (daily.getNight().getWind().getDegree() != null && daily.getNight().getWind().getDegree().getDegree() != null) {
                    nightIcon.rotate(daily.getNight().getWind().getDegree().getDegree() + 180);
                }
                nightIcon.setColorFilter(new PorterDuffColorFilter(nighttimeWindColor, PorterDuff.Mode.SRC_ATOP));
                dailyItem.setNightIconDrawable(nightIcon);
            }

            dailyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    public DailyWindAdapter(GeoActivity activity, Location location, SpeedUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mSpeedUnit = unit;

        mHighestWindSpeed = 0;
        Float daytimeWindSpeed;
        Float nighttimeWindSpeed;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            if (weather.getDailyForecast().get(i).getDay() != null && weather.getDailyForecast().get(i).getDay().getWind() != null) {
                daytimeWindSpeed = weather.getDailyForecast().get(i).getDay().getWind().getSpeed();
                if (daytimeWindSpeed != null && daytimeWindSpeed > mHighestWindSpeed){
                    mHighestWindSpeed = daytimeWindSpeed;
                }
            }
            if (weather.getDailyForecast().get(i).getNight() != null && weather.getDailyForecast().get(i).getNight().getWind() != null) {
                nighttimeWindSpeed = weather.getDailyForecast().get(i).getNight().getWind().getSpeed();
                if (nighttimeWindSpeed != null && nighttimeWindSpeed > mHighestWindSpeed) {
                    mHighestWindSpeed = nighttimeWindSpeed;
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_daily, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsDailyTrendAdapter.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        return getLocation().getWeather().getDailyForecast().size();
    }

    @Override
    public boolean isValid(Location location) {
        return mHighestWindSpeed > 0;
    }

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.tag_wind);
    }

    @Override
    public void bindBackgroundForHost(TrendRecyclerView host) {
        SpeedUnit unit = SettingsManager.getInstance(getActivity()).getSpeedUnit();

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Wind.WIND_SPEED_3,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                        getActivity().getString(R.string.wind_3),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Wind.WIND_SPEED_7,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                        getActivity().getString(R.string.wind_7),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Wind.WIND_SPEED_3,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                        getActivity().getString(R.string.wind_3),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Wind.WIND_SPEED_7,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                        getActivity().getString(R.string.wind_7),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        host.setData(keyLineList, mHighestWindSpeed, -mHighestWindSpeed);
    }
}