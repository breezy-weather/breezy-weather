package wangdaye.com.geometricweather.ui.widget.trend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Precipitation;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.dialog.HourlyWeatherDialog;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.ui.widget.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.ui.widget.trend.item.HourlyTrendItemView;

/**
 * Hourly precipitation adapter.
 * */

public abstract class HourlyPrecipitationAdapter extends TrendRecyclerViewAdapter<HourlyPrecipitationAdapter.ViewHolder> {

    private GeoActivity activity;

    private Weather weather;
    private ResourceProvider provider;
    private MainColorPicker picker;
    private PrecipitationUnit unit;

    private float highestPrecipitation;

    private int[] themeColors;

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

            Float precipitation = weather.getHourlyForecast().get(position).getPrecipitation().getTotal();
            polylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    precipitation,
                    unit.getPrecipitationTextWithoutUnit(precipitation == null ? 0 : precipitation),
                    highestPrecipitation,
                    0f
            );
            polylineAndHistogramView.setLineColors(
                    hourly.getPrecipitation().getPrecipitationColor(context),
                    hourly.getPrecipitation().getPrecipitationColor(context),
                    picker.getLineColor(context)
            );
            polylineAndHistogramView.setShadowColors(
                    themeColors[picker.isLightTheme() ? 1 : 2], themeColors[2], picker.isLightTheme());
            polylineAndHistogramView.setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            polylineAndHistogramView.setHistogramAlpha(picker.isLightTheme() ? 1f : 0.5f);

            hourlyItem.setOnClickListener(v -> {
                if (activity.isForeground()) {
                    HourlyWeatherDialog dialog = new HourlyWeatherDialog();
                    dialog.setData(weather, getAdapterPosition(), themeColors[0]);
                    dialog.setColorPicker(picker);
                    dialog.show(activity.getSupportFragmentManager(), null);
                }
            });
        }
    }

    public HourlyPrecipitationAdapter(GeoActivity activity, TrendRecyclerView parent,
                                      @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                      int itemCountPerLine, @Px float itemHeight,
                                      @NonNull Weather weather, int[] themeColors,
                                      ResourceProvider provider, MainColorPicker picker, PrecipitationUnit unit) {
        super(activity, parent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight);
        this.activity = activity;

        this.weather = weather;
        this.provider = provider;
        this.picker = picker;
        this.unit = unit;

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

        this.themeColors = themeColors;

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
        parent.setLineColor(picker.getLineColor(activity));
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
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return weather.getHourlyForecast().size();
    }
}