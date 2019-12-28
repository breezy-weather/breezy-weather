package wangdaye.com.geometricweather.main.adapter.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Precipitation;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.chart.DoubleHistogramView;
import wangdaye.com.geometricweather.ui.widget.trend.item.DailyTrendItemView;

/**
 * Daily precipitation adapter.
 * */
public class DailyPrecipitationAdapter extends AbsDailyTrendAdapter<DailyPrecipitationAdapter.ViewHolder> {

    private Weather weather;
    private TimeZone timeZone;
    private ResourceProvider provider;
    private MainThemePicker picker;
    private PrecipitationUnit unit;

    private float highestPrecipitation;

    class ViewHolder extends RecyclerView.ViewHolder {

        private DailyTrendItemView dailyItem;
        private DoubleHistogramView doubleHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            dailyItem = itemView.findViewById(R.id.item_trend_daily);
            dailyItem.setParent(getTrendParent());
            dailyItem.setWidth(getItemWidth());
            dailyItem.setHeight(getItemHeight());

            doubleHistogramView = new DoubleHistogramView(itemView.getContext());
            dailyItem.setChartItemView(doubleHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(int position) {
            Context context = itemView.getContext();
            Daily daily = weather.getDailyForecast().get(position);

            if (daily.isToday(timeZone)) {
                dailyItem.setWeekText(context.getString(R.string.today));
            } else {
                dailyItem.setWeekText(daily.getWeek(context));
            }

            dailyItem.setDateText(daily.getShortDate(context));

            dailyItem.setTextColor(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );

            dailyItem.setDayIconDrawable(
                    ResourceHelper.getWeatherIcon(provider, daily.day().getWeatherCode(), true));

            Float daytimePrecipitation = weather.getDailyForecast().get(position).day().getPrecipitation().getTotal();
            Float nighttimePrecipitation = weather.getDailyForecast().get(position).night().getPrecipitation().getTotal();
            doubleHistogramView.setData(
                    weather.getDailyForecast().get(position).day().getPrecipitation().getTotal(),
                    weather.getDailyForecast().get(position).night().getPrecipitation().getTotal(),
                    unit.getPrecipitationTextWithoutUnit(daytimePrecipitation == null ? 0 : daytimePrecipitation),
                    unit.getPrecipitationTextWithoutUnit(nighttimePrecipitation == null ? 0 : nighttimePrecipitation),
                    highestPrecipitation
            );
            doubleHistogramView.setLineColors(
                    daily.day().getPrecipitation().getPrecipitationColor(context),
                    daily.night().getPrecipitation().getPrecipitationColor(context),
                    picker.getLineColor(context)
            );
            doubleHistogramView.setTextColors(picker.getTextContentColor(context));
            doubleHistogramView.setHistogramAlphas(1f, 0.5f);

            dailyItem.setNightIconDrawable(
                    ResourceHelper.getWeatherIcon(provider, daily.night().getWeatherCode(), false));

            dailyItem.setOnClickListener(v -> onItemClicked(getAdapterPosition()));
        }
    }

    @SuppressLint("SimpleDateFormat")
    public DailyPrecipitationAdapter(GeoActivity activity, TrendRecyclerView parent,
                                     @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                                     String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                                     ResourceProvider provider, MainThemePicker picker, PrecipitationUnit unit) {
        super(activity, parent, formattedId, parentWidth, parentHeight, itemCountPerLine);

        this.weather = weather;
        this.timeZone = timeZone;
        this.provider = provider;
        this.picker = picker;
        this.unit = unit;

        highestPrecipitation = Integer.MIN_VALUE;
        Float daytimePrecipitation;
        Float nighttimePrecipitation;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            daytimePrecipitation = weather.getDailyForecast().get(i).day().getPrecipitation().getTotal();
            nighttimePrecipitation = weather.getDailyForecast().get(i).night().getPrecipitation().getTotal();
            if (daytimePrecipitation != null && daytimePrecipitation > highestPrecipitation) {
                highestPrecipitation = daytimePrecipitation;
            }
            if (nighttimePrecipitation != null && nighttimePrecipitation > highestPrecipitation) {
                highestPrecipitation = nighttimePrecipitation;
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
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Precipitation.PRECIPITATION_LIGHT,
                        activity.getString(R.string.precipitation_light),
                        unit.getPrecipitationTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Precipitation.PRECIPITATION_HEAVY,
                        activity.getString(R.string.precipitation_heavy),
                        unit.getPrecipitationTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        parent.setLineColor(picker.getLineColor(activity));
        parent.setData(keyLineList, highestPrecipitation, -highestPrecipitation);
    }

    @NonNull
    @Override
    public DailyPrecipitationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_daily, parent, false);
        return new DailyPrecipitationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyPrecipitationAdapter.ViewHolder holder, int position) {
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return weather.getDailyForecast().size();
    }
}