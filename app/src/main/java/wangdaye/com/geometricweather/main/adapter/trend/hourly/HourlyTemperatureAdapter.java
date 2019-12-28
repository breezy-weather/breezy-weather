package wangdaye.com.geometricweather.main.adapter.trend.hourly;

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

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.ui.widget.trend.item.HourlyTrendItemView;

/**
 * Hourly temperature adapter.
 * */

public abstract class HourlyTemperatureAdapter extends AbsHourlyTrendAdapter<HourlyTemperatureAdapter.ViewHolder> {

    private Weather weather;
    private ResourceProvider provider;
    private MainThemePicker picker;
    private TemperatureUnit unit;

    private float[] temperatures;
    private int highestTemperature;
    private int lowestTemperature;

    private boolean showPrecipitationProbability;

    class ViewHolder extends RecyclerView.ViewHolder {

        private HourlyTrendItemView hourlyItem;
        private PolylineAndHistogramView polylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            hourlyItem = itemView.findViewById(R.id.item_trend_hourly);
            hourlyItem.setParent(getTrendParent());
            hourlyItem.setWidth(getItemWidth());
            hourlyItem.setHeight(getItemHeight());

            polylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(polylineAndHistogramView);
        }

        void onBindView(int position) {
            Context context = itemView.getContext();
            Hourly hourly = weather.getHourlyForecast().get(position);

            hourlyItem.setHourText(hourly.getHour(context));

            hourlyItem.setTextColor(picker.getTextContentColor(context));

            hourlyItem.setIconDrawable(
                    ResourceHelper.getWeatherIcon(provider, hourly.getWeatherCode(), hourly.isDaylight())
            );

            Float precipitationProbability = hourly.getPrecipitationProbability().getTotal();
            float p = precipitationProbability == null ? 0 : precipitationProbability;
            if (!showPrecipitationProbability) {
                p = 0;
            }
            polylineAndHistogramView.setData(
                    buildTemperatureArrayForItem(temperatures, position),
                    null,
                    getShortTemperatureString(weather, position, unit),
                    null,
                    (float) highestTemperature,
                    (float) lowestTemperature,
                    p < 5 ? null : p,
                    p < 5 ? null : ((int) p + "%"),
                    100f,
                    0f
            );
            int[] themeColors = picker.getWeatherThemeColors();
            polylineAndHistogramView.setLineColors(
                    themeColors[picker.isLightTheme() ? 1 : 2], themeColors[2], picker.getLineColor(context));
            polylineAndHistogramView.setShadowColors(
                    themeColors[picker.isLightTheme() ? 1 : 2], themeColors[2], picker.isLightTheme());
            polylineAndHistogramView.setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            polylineAndHistogramView.setHistogramAlpha(picker.isLightTheme() ? 0.2f : 0.5f);

            hourlyItem.setOnClickListener(v -> onItemClicked(getAdapterPosition()));
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

    public HourlyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent, @NonNull Weather weather,
                                    @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                                    ResourceProvider provider, MainThemePicker picker, TemperatureUnit unit) {
        this(activity, parent, weather, true, parentWidth, parentHeight,
                itemCountPerLine, provider, picker, unit);
    }

    public HourlyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent, @NonNull Weather weather,
                                    boolean showPrecipitationProbability,
                                    @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                                    ResourceProvider provider, MainThemePicker picker, TemperatureUnit unit) {
        super(activity, parent, weather, picker, parentWidth, parentHeight, itemCountPerLine);

        this.weather = weather;
        this.provider = provider;
        this.picker = picker;
        this.unit = unit;

        this.temperatures = new float[Math.max(0, weather.getHourlyForecast().size() * 2 - 1)];
        for (int i = 0; i < temperatures.length; i += 2) {
            temperatures[i] = getTemperatureC(weather, i / 2);
        }
        for (int i = 1; i < temperatures.length; i += 2) {
            temperatures[i] = (temperatures[i - 1] + temperatures[i + 1]) * 0.5F;
        }

        highestTemperature = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        lowestTemperature = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < weather.getHourlyForecast().size(); i ++) {
            if (getTemperatureC(weather, i) > highestTemperature) {
                highestTemperature = getTemperatureC(weather, i);
            }
            if (getTemperatureC(weather, i) < lowestTemperature) {
                lowestTemperature = getTemperatureC(weather, i);
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
                .inflate(R.layout.item_trend_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return weather.getHourlyForecast().size();
    }

    protected abstract int getTemperatureC(Weather weather, int index);

    protected abstract int getTemperature(Weather weather, int index, TemperatureUnit unit);

    protected abstract String getTemperatureString(Weather weather, int index, TemperatureUnit unit);

    protected abstract String getShortTemperatureString(Weather weather, int index, TemperatureUnit unit);
}