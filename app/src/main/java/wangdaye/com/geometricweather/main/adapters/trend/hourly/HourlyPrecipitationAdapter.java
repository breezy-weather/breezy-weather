package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.content.Context;
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
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;

/**
 * Hourly precipitation adapter.
 * */

public class HourlyPrecipitationAdapter extends AbsHourlyTrendAdapter {

    private final ResourceProvider mResourceProvider;
    private final PrecipitationUnit mPrecipitationUnit;
    private float mHighestPrecipitation;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_precipitation));

            super.onBindView(activity, location, talkBackBuilder, position);

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
                        .append(mPrecipitationUnit.getValueVoice(activity, precipitation));
            } else {
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.content_des_no_precipitation));
            }

            mPolylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    precipitation,
                    mPrecipitationUnit.getValueTextWithoutUnit(precipitation),
                    mHighestPrecipitation,
                    0f
            );
            mPolylineAndHistogramView.setLineColors(
                    hourly.getPrecipitation().getPrecipitationColor(activity),
                    hourly.getPrecipitation().getPrecipitationColor(activity),
                    MainThemeColorProvider.getColor(location, R.attr.colorOutline)
            );
            int[] themeColors = ThemeManager
                    .getInstance(itemView.getContext())
                    .getWeatherThemeDelegate()
                    .getThemeColors(
                            itemView.getContext(),
                            WeatherViewController.getWeatherKind(location.getWeather()),
                            location.isDaylight()
                    );
            boolean lightTheme = MainThemeColorProvider.isLightTheme(itemView.getContext(), location);
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[lightTheme ? 1 : 2],
                    themeColors[2],
                    lightTheme
            );
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            );
            mPolylineAndHistogramView.setHistogramAlpha(lightTheme ? 1f : 0.5f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    public HourlyPrecipitationAdapter(GeoActivity activity,
                                      Location location,
                                      ResourceProvider provider,
                                      PrecipitationUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mResourceProvider = provider;
        mPrecipitationUnit = unit;

        mHighestPrecipitation = 0;
        Float precipitation;
        for (int i = weather.getHourlyForecast().size() - 1; i >= 0; i --) {
            precipitation = weather.getHourlyForecast().get(i).getPrecipitation().getTotal();
            if (precipitation != null && precipitation > mHighestPrecipitation) {
                mHighestPrecipitation = precipitation;
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
        assert getLocation().getWeather() != null;
        return getLocation().getWeather().getHourlyForecast().size();
    }

    @Override
    public boolean isValid(Location location) {
        return mHighestPrecipitation > 0;
    }

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.tag_precipitation);
    }

    @Override
    public void bindBackgroundForHost(TrendRecyclerView host) {
        PrecipitationUnit unit = SettingsManager.getInstance(getActivity()).getPrecipitationUnit();

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Precipitation.PRECIPITATION_LIGHT,
                        getActivity().getString(R.string.precipitation_light),
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Precipitation.PRECIPITATION_HEAVY,
                        getActivity().getString(R.string.precipitation_heavy),
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        host.setData(keyLineList, mHighestPrecipitation, 0f);
    }
}