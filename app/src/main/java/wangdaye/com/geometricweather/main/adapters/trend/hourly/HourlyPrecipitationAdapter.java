package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;

/**
 * Hourly precipitation adapter.
 * */

public class HourlyPrecipitationAdapter extends AbsHourlyTrendAdapter<HourlyPrecipitationAdapter.ViewHolder> {

    private final ResourceProvider mResourceProvider;
    private final MainThemeManager mThemeManager;
    private final PrecipitationUnit mPrecipitationUnit;

    private float highestPrecipitation;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        void onBindView(GeoActivity activity, Location location, MainThemeManager themeManager, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_precipitation));

            super.onBindView(activity, location, themeManager, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            hourlyItem.setIconDrawable(
                    ResourceHelper.getWeatherIcon(mResourceProvider, hourly.getWeatherCode(), hourly.isDaylight())
            );

            Float precipitation = weather.getHourlyForecast().get(position).getPrecipitation().getTotal();
            precipitation = precipitation == null ? 0 : precipitation;

            if (precipitation != 0) {
                talkBackBuilder.append(", ")
                        .append(mPrecipitationUnit.getPrecipitationVoice(activity, precipitation));
            } else {
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.content_des_no_precipitation));
            }

            mPolylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    precipitation,
                    mPrecipitationUnit.getPrecipitationTextWithoutUnit(precipitation),
                    highestPrecipitation,
                    0f
            );
            mPolylineAndHistogramView.setLineColors(
                    hourly.getPrecipitation().getPrecipitationColor(activity),
                    hourly.getPrecipitation().getPrecipitationColor(activity),
                    mThemeManager.getLineColor(activity)
            );
            int[] themeColors = mThemeManager.getWeatherThemeColors();
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[mThemeManager.isLightTheme() ? 1 : 2], themeColors[2], mThemeManager.isLightTheme());
            mPolylineAndHistogramView.setTextColors(
                    mThemeManager.getTextContentColor(activity),
                    mThemeManager.getTextSubtitleColor(activity)
            );
            mPolylineAndHistogramView.setHistogramAlpha(mThemeManager.isLightTheme() ? 1f : 0.5f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    public HourlyPrecipitationAdapter(GeoActivity activity,
                                      TrendRecyclerView parent,
                                      Location location,
                                      ResourceProvider provider,
                                      MainThemeManager themeManager,
                                      PrecipitationUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mResourceProvider = provider;
        mThemeManager = themeManager;
        mPrecipitationUnit = unit;

        highestPrecipitation = Integer.MIN_VALUE;
        Float precipitation;
        for (int i = weather.getHourlyForecast().size() - 1; i >= 0; i --) {
            precipitation = weather.getHourlyForecast().get(i).getPrecipitation().getTotal();
            if (precipitation != null && precipitation > highestPrecipitation) {
                highestPrecipitation = precipitation;
            }
        }
        if (highestPrecipitation == 0) {
            highestPrecipitation = Precipitation.PRECIPITATION_HEAVY;
        }

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Precipitation.PRECIPITATION_LIGHT,
                        activity.getString(R.string.precipitation_light),
                        unit.getPrecipitationTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Precipitation.PRECIPITATION_HEAVY,
                        activity.getString(R.string.precipitation_heavy),
                        unit.getPrecipitationTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        parent.setLineColor(mThemeManager.getLineColor(activity));
        parent.setData(keyLineList, highestPrecipitation, 0f);
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
        holder.onBindView(getActivity(), getLocation(), mThemeManager, position);
    }

    @Override
    public int getItemCount() {
        assert getLocation().getWeather() != null;
        return getLocation().getWeather().getHourlyForecast().size();
    }
}