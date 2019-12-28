package wangdaye.com.geometricweather.main.adapter.trend.hourly;

import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.main.dialog.HourlyWeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendParent;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;

public abstract class AbsHourlyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private GeoActivity activity;
    private Weather weather;
    private MainThemePicker picker;

    public AbsHourlyTrendAdapter(GeoActivity activity, TrendParent trendParent, Weather weather,
                                 MainThemePicker picker,
                                 @Px float parentWidth, @Px float parentHeight, int itemCountPerLine) {
        super(trendParent, parentWidth, parentHeight, itemCountPerLine);
        this.activity = activity;
        this.weather = weather;
        this.picker = picker;
    }

    protected void onItemClicked(int adapterPosition) {
        if (activity.isForeground()) {
            HourlyWeatherDialog dialog = new HourlyWeatherDialog();
            dialog.setData(weather, adapterPosition, picker.getWeatherThemeColors()[0]);
            dialog.setColorPicker(picker);
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }
}
