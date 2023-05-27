package wangdaye.com.geometricweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
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
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.DoubleHistogramView;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;

/**
 * Daily precipitation adapter.
 * */
public class DailyPrecipitationAdapter extends AbsDailyTrendAdapter {

    private final ResourceProvider mResourceProvider;
    private final PrecipitationUnit mPrecipitationUnit;
    private float mHighestPrecipitation;

    class ViewHolder extends AbsDailyTrendAdapter.ViewHolder {

        private final DoubleHistogramView mDoubleHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mDoubleHistogramView = new DoubleHistogramView(itemView.getContext());
            dailyItem.setChartItemView(mDoubleHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_precipitation));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Daily daily = weather.getDailyForecast().get(position);

            Float daytimePrecipitation = weather.getDailyForecast().get(position).day().getPrecipitation().getTotal();
            Float nighttimePrecipitation = weather.getDailyForecast().get(position).night().getPrecipitation().getTotal();

            daytimePrecipitation = daytimePrecipitation == null ? 0 : daytimePrecipitation;
            nighttimePrecipitation = nighttimePrecipitation == null ? 0 : nighttimePrecipitation;

            if (daytimePrecipitation != 0 || nighttimePrecipitation != 0) {
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.daytime))
                        .append(" : ")
                        .append(mPrecipitationUnit.getValueVoice(activity, daytimePrecipitation));
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.nighttime))
                        .append(" : ")
                        .append(mPrecipitationUnit.getValueVoice(activity, nighttimePrecipitation));
            } else {
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.content_des_no_precipitation));
            }

            dailyItem.setDayIconDrawable(
                    ResourceHelper.getWeatherIcon(mResourceProvider, daily.day().getWeatherCode(), true));

            mDoubleHistogramView.setData(
                    weather.getDailyForecast().get(position).day().getPrecipitation().getTotal(),
                    weather.getDailyForecast().get(position).night().getPrecipitation().getTotal(),
                    mPrecipitationUnit.getValueTextWithoutUnit(daytimePrecipitation),
                    mPrecipitationUnit.getValueTextWithoutUnit(nighttimePrecipitation),
                    mHighestPrecipitation
            );
            mDoubleHistogramView.setLineColors(
                    daily.day().getPrecipitation().getPrecipitationColor(activity),
                    daily.night().getPrecipitation().getPrecipitationColor(activity),
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            );
            mDoubleHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
            );
            mDoubleHistogramView.setHistogramAlphas(1f, 0.5f);

            dailyItem.setNightIconDrawable(
                    ResourceHelper.getWeatherIcon(mResourceProvider, daily.night().getWeatherCode(), false));

            dailyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    @SuppressLint("SimpleDateFormat")
    public DailyPrecipitationAdapter(GeoActivity activity,
                                     Location location,
                                     ResourceProvider provider,
                                     PrecipitationUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mResourceProvider = provider;
        mPrecipitationUnit = unit;

        mHighestPrecipitation = 0;
        Float daytimePrecipitation;
        Float nighttimePrecipitation;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            daytimePrecipitation = weather.getDailyForecast().get(i).day().getPrecipitation().getTotal();
            nighttimePrecipitation = weather.getDailyForecast().get(i).night().getPrecipitation().getTotal();
            if (daytimePrecipitation != null && daytimePrecipitation > mHighestPrecipitation) {
                mHighestPrecipitation = daytimePrecipitation;
            }
            if (nighttimePrecipitation != null && nighttimePrecipitation > mHighestPrecipitation) {
                mHighestPrecipitation = nighttimePrecipitation;
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
        assert getLocation().getWeather() != null;
        return getLocation().getWeather().getDailyForecast().size();
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
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                        getActivity().getString(R.string.precipitation_light),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Precipitation.PRECIPITATION_HEAVY,
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                        getActivity().getString(R.string.precipitation_heavy),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Precipitation.PRECIPITATION_LIGHT,
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                        getActivity().getString(R.string.precipitation_light),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Precipitation.PRECIPITATION_HEAVY,
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                        getActivity().getString(R.string.precipitation_heavy),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        host.setData(keyLineList, mHighestPrecipitation, -mHighestPrecipitation);
    }
}