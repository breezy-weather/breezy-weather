package wangdaye.com.geometricweather.main.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.dialog.GeoBottomSheetDialogFragment;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWeatherAdapter;

/**
 * Daily weather dialog.
 * */

public class DailyWeatherDialog extends GeoBottomSheetDialogFragment {

    private CoordinatorLayout container;
    private MainColorPicker colorPicker;

    private Weather weather;
    private int position;

    @ColorInt private int weatherColor;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_weather_daily, null, false);
        this.initWidget(view);
        dialog.setContentView(view);
        setBehavior(BottomSheetBehavior.from((View) view.getParent()));
        return dialog;
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @SuppressLint("SetTextI18n")
    private void initWidget(View view) {
        if (getActivity() == null) {
            return;
        }

        Daily daily = weather.getDailyForecast().get(position);

        this.container = view.findViewById(R.id.dialog_weather_daily_container);
        container.setBackgroundColor(colorPicker.getRootColor(getActivity()));

        RecyclerView recyclerView = view.findViewById(R.id.dialog_weather_daily_recyclerView);

        DailyWeatherAdapter dailyWeatherAdapter = new DailyWeatherAdapter(getActivity(), daily, weatherColor, 3);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        gridLayoutManager.setSpanSizeLookup(dailyWeatherAdapter.spanSizeLookup);
        recyclerView.setAdapter(dailyWeatherAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setData(Weather weather, int position, @ColorInt int weatherColor) {
        this.weather = weather;
        this.position = position;
        this.weatherColor = weatherColor;
    }

    public void setColorPicker(@NonNull MainColorPicker colorPicker) {
        this.colorPicker = colorPicker;
    }
}
