package wangdaye.com.geometricweather.ui.widget.trend.adapter.hourly;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.dialog.HourlyWeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendParent;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public abstract class AbsHourlyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private GeoActivity activity;
    private Weather weather;
    private MainColorPicker picker;
    private @ColorInt int themeColor;

    public AbsHourlyTrendAdapter(GeoActivity activity, TrendParent trendParent,
                                 Weather weather, MainColorPicker picker, @ColorInt int themeColor,
                                 float cardMarginsVertical, float cardMarginsHorizontal,
                                 int itemCountPerLine, float itemHeight) {
        super(activity, trendParent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight);
        this.activity = activity;
        this.weather = weather;
        this.picker = picker;
        this.themeColor = themeColor;
    }

    protected void onItemClicked(int adapterPosition) {
        if (activity.isForeground()) {
            HourlyWeatherDialog dialog = new HourlyWeatherDialog();
            dialog.setData(weather, adapterPosition, themeColor);
            dialog.setColorPicker(picker);
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }
}
