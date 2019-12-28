package wangdaye.com.geometricweather.main.adapter.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.ui.widget.trend.item.DailyTrendItemView;

/**
 * Daily temperature adapter.
 * */

public abstract class DailyTemperatureAdapter extends AbsDailyTrendAdapter<DailyTemperatureAdapter.ViewHolder> {

    private Weather weather;
    private TimeZone timeZone;
    private ResourceProvider provider;
    private MainThemePicker picker;
    private TemperatureUnit unit;

    private float[] daytimeTemperatures;
    private float[] nighttimeTemperatures;
    private int highestTemperature;
    private int lowestTemperature;

    private boolean showPrecipitationProbability;

    class ViewHolder extends RecyclerView.ViewHolder {

        private DailyTrendItemView dailyItem;
        private PolylineAndHistogramView polylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            dailyItem = itemView.findViewById(R.id.item_trend_daily);
            dailyItem.setParent(getTrendParent());
            dailyItem.setWidth(getItemWidth());
            dailyItem.setHeight(getItemHeight());

            polylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            dailyItem.setChartItemView(polylineAndHistogramView);
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

            Float daytimePrecipitationProbability = daily.day().getPrecipitationProbability().getTotal();
            Float nighttimePrecipitationProbability = daily.night().getPrecipitationProbability().getTotal();
            float p = Math.max(
                    daytimePrecipitationProbability == null ? 0 : daytimePrecipitationProbability,
                    nighttimePrecipitationProbability == null ? 0 : nighttimePrecipitationProbability
            );
            if (!showPrecipitationProbability) {
                p = 0;
            }
            polylineAndHistogramView.setData(
                    buildTemperatureArrayForItem(daytimeTemperatures, position),
                    buildTemperatureArrayForItem(nighttimeTemperatures, position),
                    getShortDaytimeTemperatureString(weather, position, unit),
                    getShortNighttimeTemperatureString(weather, position, unit),
                    (float) highestTemperature,
                    (float) lowestTemperature,
                    p < 5 ? null : p,
                    p < 5 ? null : ((int) p + "%"),
                    100f,
                    0f
            );
            int[] themeColors = picker.getWeatherThemeColors();
            polylineAndHistogramView.setLineColors(
                    themeColors[1], themeColors[2], picker.getLineColor(context));
            polylineAndHistogramView.setShadowColors(
                    themeColors[1], themeColors[2], picker.isLightTheme());
            polylineAndHistogramView.setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            polylineAndHistogramView.setHistogramAlpha(picker.isLightTheme() ? 0.2f : 0.5f);

            dailyItem.setNightIconDrawable(
                    ResourceHelper.getWeatherIcon(provider, daily.night().getWeatherCode(), false));

            dailyItem.setOnClickListener(v -> onItemClicked(getAdapterPosition()));
        }

        @Size(3)
        private Float[] buildTemperatureArrayForItem(float[] temps, int adapterPosition) {
            Float[] a = new Float[3];
            a[1] = temps[2 * adapterPosition];
            if (2 * adapterPosition - 1 < 0) {
                a[0] = null;
            } else {
                a[0] = temps[2 * adapterPosition - 1];
            }
            if (2 * adapterPosition + 1 >= temps.length) {
                a[2] = null;
            } else {
                a[2] = temps[2 * adapterPosition + 1];
            }
            return a;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public DailyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent,
                                   @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                                   String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                                   ResourceProvider provider, MainThemePicker picker, TemperatureUnit unit) {
        this(activity, parent, parentWidth, parentHeight, itemCountPerLine,
                formattedId, weather, timeZone, true, provider, picker, unit);
    }

    @SuppressLint("SimpleDateFormat")
    public DailyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent,
                                   @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                                   String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                                   boolean showPrecipitationProbability,
                                   ResourceProvider provider, MainThemePicker picker, TemperatureUnit unit) {
        super(activity, parent, formattedId, parentWidth, parentHeight, itemCountPerLine);

        this.weather = weather;
        this.timeZone = timeZone;
        this.provider = provider;
        this.picker = picker;
        this.unit = unit;

        this.daytimeTemperatures = new float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < daytimeTemperatures.length; i += 2) {
            daytimeTemperatures[i] = getDaytimeTemperatureC(weather, i / 2);
        }
        for (int i = 1; i < daytimeTemperatures.length; i += 2) {
            daytimeTemperatures[i] = (daytimeTemperatures[i - 1] + daytimeTemperatures[i + 1]) * 0.5F;
        }

        this.nighttimeTemperatures = new float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < nighttimeTemperatures.length; i += 2) {
            nighttimeTemperatures[i] = getNighttimeTemperatureC(weather, i / 2);
        }
        for (int i = 1; i < nighttimeTemperatures.length; i += 2) {
            nighttimeTemperatures[i] = (nighttimeTemperatures[i - 1] + nighttimeTemperatures[i + 1]) * 0.5F;
        }

        highestTemperature = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        lowestTemperature = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
            if (getDaytimeTemperatureC(weather, i) > highestTemperature) {
                highestTemperature = getDaytimeTemperatureC(weather, i);
            }
            if (getNighttimeTemperatureC(weather, i) < lowestTemperature) {
                lowestTemperature = getNighttimeTemperatureC(weather, i);
            }
        }

        this.showPrecipitationProbability = showPrecipitationProbability;

        parent.setLineColor(picker.getLineColor(activity));
        if (weather.getYesterday() == null) {
            parent.setData(null,0, 0);
        } else {
            List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
            keyLineList.add(
                    new TrendRecyclerView.KeyLine(
                            weather.getYesterday().getDaytimeTemperature(),
                            Temperature.getShortTemperature(weather.getYesterday().getDaytimeTemperature(), unit),
                            activity.getString(R.string.yesterday),
                            TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                    )
            );
            keyLineList.add(
                    new TrendRecyclerView.KeyLine(
                            weather.getYesterday().getNighttimeTemperature(),
                            Temperature.getShortTemperature(weather.getYesterday().getNighttimeTemperature(), unit),
                            activity.getString(R.string.yesterday),
                            TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                    )
            );
            parent.setData(keyLineList, highestTemperature, lowestTemperature);
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return weather.getDailyForecast().size();
    }

    protected abstract int getDaytimeTemperatureC(Weather weather, int index);

    protected abstract int getNighttimeTemperatureC(Weather weather, int index);
    
    protected abstract int getDaytimeTemperature(Weather weather, int index, TemperatureUnit unit);

    protected abstract int getNighttimeTemperature(Weather weather, int index, TemperatureUnit unit);

    protected abstract String getDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit);

    protected abstract String getNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit);

    protected abstract String getShortDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit);

    protected abstract String getShortNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit);
}