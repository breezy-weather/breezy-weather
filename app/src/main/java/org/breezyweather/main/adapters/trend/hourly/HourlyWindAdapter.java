package org.breezyweather.main.adapters.trend.hourly;

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
import org.breezyweather.common.basic.models.weather.Hourly;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.basic.models.weather.Wind;
import org.breezyweather.common.ui.images.RotateDrawable;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;

/**
 * Hourly wind adapter.
 **/
public class HourlyWindAdapter extends AbsHourlyTrendAdapter {

    private final SpeedUnit mSpeedUnit;
    private float mHighestWindSpeed;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);

            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_wind));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            if (hourly.getWind() != null) {
                talkBackBuilder
                        .append(", ").append(activity.getString(R.string.tag_wind))
                        .append(" : ").append(hourly.getWind().getWindDescription(activity, mSpeedUnit));

                int daytimeWindColor = hourly.getWind().getWindColor(activity);

                RotateDrawable dayIcon = hourly.getWind().isValidSpeed()
                        ? new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_navigation))
                        : new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium));
                if (hourly.getWind().getDegree() != null) {
                    dayIcon.rotate(hourly.getWind().getDegree().getDegree() + 180);
                }
                dayIcon.setColorFilter(new PorterDuffColorFilter(daytimeWindColor, PorterDuff.Mode.SRC_ATOP));
                hourlyItem.setIconDrawable(dayIcon);
                Float daytimeWindSpeed = weather.getHourlyForecast().get(position).getWind().getSpeed();
                mPolylineAndHistogramView.setData(
                        null, null,
                        null, null,
                        null, null,
                        weather.getHourlyForecast().get(position).getWind().getSpeed(),
                        mSpeedUnit.getValueTextWithoutUnit(daytimeWindSpeed == null ? 0 : daytimeWindSpeed),
                        mHighestWindSpeed, 0f
                );
                mPolylineAndHistogramView.setLineColors(
                        daytimeWindColor,
                        daytimeWindColor,
                        MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                );
            }

            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            );
            mPolylineAndHistogramView.setHistogramAlpha(1f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    public HourlyWindAdapter(GeoActivity activity, Location location, SpeedUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mSpeedUnit = unit;

        mHighestWindSpeed = 0;
        Float daytimeWindSpeed;
        for (int i = weather.getHourlyForecast().size() - 1; i >= 0; i --) {
            if (weather.getHourlyForecast().get(i).getWind() != null) {
                daytimeWindSpeed = weather.getHourlyForecast().get(i).getWind().getSpeed();
                if (daytimeWindSpeed != null && daytimeWindSpeed > mHighestWindSpeed) {
                    mHighestWindSpeed = daytimeWindSpeed;
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsHourlyTrendAdapter.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        return getLocation().getWeather().getHourlyForecast().size();
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
        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Wind.WIND_SPEED_3,
                        mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                        getActivity().getString(R.string.wind_3),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Wind.WIND_SPEED_7,
                        mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                        getActivity().getString(R.string.wind_7),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Wind.WIND_SPEED_3,
                        mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                        getActivity().getString(R.string.wind_3),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Wind.WIND_SPEED_7,
                        mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                        getActivity().getString(R.string.wind_7),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        host.setData(keyLineList, mHighestWindSpeed, 0);
    }
}