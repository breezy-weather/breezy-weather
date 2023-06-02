package wangdaye.com.geometricweather.main.adapters.trend.daily;

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

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.SpeedUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;
import wangdaye.com.geometricweather.common.ui.images.RotateDrawable;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.DoubleHistogramView;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.settings.SettingsManager;

/**
 * Daily wind adapter.
 * */
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

            talkBackBuilder
                    .append(", ").append(activity.getString(R.string.daytime))
                    .append(" : ").append(daily.day().getWind().getWindDescription(activity, mSpeedUnit))
                    .append(", ").append(activity.getString(R.string.nighttime))
                    .append(" : ").append(daily.night().getWind().getWindDescription(activity, mSpeedUnit));

            int daytimeWindColor = daily.day().getWind().getWindColor(activity);
            int nighttimeWindColor = daily.night().getWind().getWindColor(activity);

            RotateDrawable dayIcon = daily.day().getWind().isValidSpeed()
                    ? new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_navigation))
                    : new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium));
            dayIcon.rotate(daily.day().getWind().getDegree().getDegree() + 180);
            dayIcon.setColorFilter(new PorterDuffColorFilter(daytimeWindColor, PorterDuff.Mode.SRC_ATOP));
            dailyItem.setDayIconDrawable(dayIcon);

            Float daytimeWindSpeed = weather.getDailyForecast().get(position).day().getWind().getSpeed();
            Float nighttimeWindSpeed = weather.getDailyForecast().get(position).night().getWind().getSpeed();
            mDoubleHistogramView.setData(
                    weather.getDailyForecast().get(position).day().getWind().getSpeed(),
                    weather.getDailyForecast().get(position).night().getWind().getSpeed(),
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

            RotateDrawable nightIcon = daily.night().getWind().isValidSpeed()
                    ? new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_navigation))
                    : new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium));
            nightIcon.rotate(daily.night().getWind().getDegree().getDegree() + 180);
            nightIcon.setColorFilter(new PorterDuffColorFilter(nighttimeWindColor, PorterDuff.Mode.SRC_ATOP));
            dailyItem.setNightIconDrawable(nightIcon);

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
            if (weather.getDailyForecast().get(i).day() != null && weather.getDailyForecast().get(i).day().getWind() != null) {
                daytimeWindSpeed = weather.getDailyForecast().get(i).day().getWind().getSpeed();
                if (daytimeWindSpeed != null && daytimeWindSpeed > mHighestWindSpeed){
                    mHighestWindSpeed = daytimeWindSpeed;
                }
            }
            if (weather.getDailyForecast().get(i).night() != null && weather.getDailyForecast().get(i).night().getWind() != null) {
                nighttimeWindSpeed = weather.getDailyForecast().get(i).night().getWind().getSpeed();
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