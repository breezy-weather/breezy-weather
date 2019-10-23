package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.NumberAnimTextView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class HeaderViewHolder extends AbstractMainViewHolder
        implements View.OnClickListener {

    private LinearLayout container;
    private NumberAnimTextView temperature;
    private TextView weather;
    private TextView aqiOrWind;

    private @Nullable Location location;
    private TemperatureUnit unit;
    private WeatherView weatherView;
    private @Nullable Disposable disposable;

    public HeaderViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                            @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                            @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                            @Px float cardRadius, @Px float cardElevation) {
        super(activity, LayoutInflater.from(activity).inflate(R.layout.container_main_header, parent, false),
                provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation);

        this.container = itemView.findViewById(R.id.container_main_header);
        this.temperature = itemView.findViewById(R.id.container_main_header_tempTxt);
        this.weather = itemView.findViewById(R.id.container_main_header_weatherTxt);
        this.aqiOrWind = itemView.findViewById(R.id.container_main_header_aqiOrWindTxt);

        this.weatherView = weatherView;
        this.disposable = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        params.height = weatherView.getFirstCardMarginTop();
        container.setLayoutParams(params);
        container.setOnClickListener(this);

        this.location = location;

        if (location.getWeather() != null) {
            this.unit = SettingsOptionManager.getInstance(context).getTemperatureUnit();
            temperature.setDuration(2000);
            temperature.setPostfixString("°");

            weather.setText(
                    location.getWeather().getCurrent().getWeatherText()
                            + ", "
                            + context.getString(R.string.feels_like)
                            + " "
                            + location.getWeather().getCurrent().getTemperature().getShortRealFeeTemperature(unit)
            );

            if (location.getWeather().getCurrent().getAirQuality().getAqiText() == null) {
                aqiOrWind.setText(
                        context.getString(R.string.wind)
                                + " - "
                                + location.getWeather().getCurrent().getWind().getShortWindDescription()
                );
            } else {
                aqiOrWind.setText(
                        context.getString(R.string.air_quality)
                                + " - "
                                + location.getWeather().getCurrent().getAirQuality().getAqiText()
                );
            }
        }
    }

    @Override
    public void executeEnterAnimator(List<Animator> pendingAnimatorList) {
        itemView.setAlpha(0f);
        Animator a = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f);
        a.setDuration(300);
        a.setStartDelay(100);
        a.setInterpolator(new FastOutSlowInInterpolator());

        pendingAnimatorList.add(a);
        disposable = Observable.timer(100, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    pendingAnimatorList.remove(a);
                    onEnterScreen();
                }).subscribe();
        a.start();
    }

    @Override
    public void onEnterScreen() {
        super.onEnterScreen();
        if (location != null && location.getWeather() != null) {
            temperature.setNumberString(
                    "0",
                    String.valueOf(unit.getTemperature(
                            location.getWeather().getCurrent().getTemperature().getTemperature()
                    ))
            );
        }
    }

    @Override
    public void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    public int getCurrentTemperatureHeight() {
        return temperature.getMeasuredHeight();
    }

    // interface.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_main_header:
                weatherView.onClick();
                break;
        }
    }
}
