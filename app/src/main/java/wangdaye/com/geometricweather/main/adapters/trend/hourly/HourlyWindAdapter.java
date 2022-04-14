package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.annotation.SuppressLint;
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
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;
import wangdaye.com.geometricweather.common.ui.images.RotateDrawable;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;

/**
 * Hourly wind adapter.
 * */
public class HourlyWindAdapter extends AbsHourlyTrendAdapter<HourlyWindAdapter.ViewHolder> {

    private final SpeedUnit mSpeedUnit;

    private float mHighestWindSpeed;
    private int mSize;

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

            talkBackBuilder
                    .append(", ").append(activity.getString(R.string.tag_wind))
                    .append(" : ").append(hourly.getWind().getWindDescription(activity, mSpeedUnit));

            int daytimeWindColor = hourly.getWind().getWindColor(activity);

            RotateDrawable dayIcon = hourly.getWind().isValidSpeed()
                    ? new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_navigation))
                    : new RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium));
            dayIcon.rotate(hourly.getWind().getDegree().getDegree() + 180);
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
                    MainThemeColorProvider.getColor(location, R.attr.colorOutline)
            );
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorCaptionText)
            );
            mPolylineAndHistogramView.setHistogramAlpha(1f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    @SuppressLint("SimpleDateFormat")
    public HourlyWindAdapter(GeoActivity activity, TrendRecyclerView parent,
                             Location location, SpeedUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mSpeedUnit = unit;

        mHighestWindSpeed = Integer.MIN_VALUE;
        Float daytimeWindSpeed;
        boolean valid = false;
        for (int i = weather.getHourlyForecast().size() - 1; i >= 0; i --) {
            daytimeWindSpeed = weather.getHourlyForecast().get(i).getWind().getSpeed();
            if (daytimeWindSpeed != null && daytimeWindSpeed > mHighestWindSpeed) {
                mHighestWindSpeed = daytimeWindSpeed;
            }
            if ((daytimeWindSpeed != null && daytimeWindSpeed != 0)
                    || valid) {
                valid = true;
                mSize++;
            }
        }
        if (mHighestWindSpeed == 0) {
            mHighestWindSpeed = Wind.WIND_SPEED_11;
        }

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Wind.WIND_SPEED_3,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                        activity.getString(R.string.wind_3),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Wind.WIND_SPEED_7,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                        activity.getString(R.string.wind_7),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Wind.WIND_SPEED_3,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                        activity.getString(R.string.wind_3),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Wind.WIND_SPEED_7,
                        unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                        activity.getString(R.string.wind_7),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        parent.setData(keyLineList, mHighestWindSpeed, 0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        return mSize;
    }
}