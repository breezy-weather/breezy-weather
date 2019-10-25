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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.List;

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

    private WeatherView weatherView;
    private int temperatureC;
    private TemperatureUnit unit;
    private @Nullable Disposable disposable;

    public HeaderViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                            @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                            @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                            @Px float cardRadius, @Px float cardElevation, @ColorInt int textColor,
                            boolean itemAnimationEnabled) {
        super(activity, LayoutInflater.from(activity).inflate(R.layout.container_main_header, parent, false),
                provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);

        this.container = itemView.findViewById(R.id.container_main_header);
        this.temperature = itemView.findViewById(R.id.container_main_header_tempTxt);
        this.weather = itemView.findViewById(R.id.container_main_header_weatherTxt);
        this.aqiOrWind = itemView.findViewById(R.id.container_main_header_aqiOrWindTxt);

        temperature.setTextColor(textColor);
        weather.setTextColor(textColor);
        aqiOrWind.setTextColor(textColor);

        this.weatherView = weatherView;
        this.temperatureC = 0;
        this.unit = null;
        this.disposable = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        params.height = weatherView.getFirstCardMarginTop();
        container.setLayoutParams(params);
        container.setOnClickListener(this);

        unit = SettingsOptionManager.getInstance(context).getTemperatureUnit();
        if (location.getWeather() != null) {
            temperatureC = location.getWeather().getCurrent().getTemperature().getTemperature();

            temperature.setEnableAnim(itemAnimationEnabled);
            temperature.setDuration(
                    (long) Math.min(
                            2000,
                            Math.max(0, Math.abs(temperatureC) / 10f * 1000)
                    )
            );
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
    protected Animator getEnterAnimator(List<Animator> pendingAnimatorList) {
        Animator a = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f);
        a.setDuration(300);
        a.setStartDelay(100);
        a.setInterpolator(new FastOutSlowInInterpolator());
        return a;
    }

    @Override
    public void onEnterScreen() {
        super.onEnterScreen();
        temperature.setNumberString(
                "0",
                String.valueOf(unit.getTemperature(temperatureC))
        );
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
