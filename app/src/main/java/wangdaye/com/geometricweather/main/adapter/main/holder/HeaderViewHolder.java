package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.NumberAnimTextView;

public class HeaderViewHolder extends AbstractMainViewHolder {

    private LinearLayout container;
    private NumberAnimTextView temperature;
    private TextView weather;
    private TextView aqiOrWind;

    private MainThemePicker themePicker;
    private int temperatureCFrom;
    private int temperatureCTo;
    private TemperatureUnit unit;
    private @Nullable Disposable disposable;

    public HeaderViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.container_main_header, parent, false));

        this.container = itemView.findViewById(R.id.container_main_header);
        this.temperature = itemView.findViewById(R.id.container_main_header_tempTxt);
        this.weather = itemView.findViewById(R.id.container_main_header_weatherTxt);
        this.aqiOrWind = itemView.findViewById(R.id.container_main_header_aqiOrWindTxt);

        this.temperatureCFrom = 0;
        this.temperatureCTo = 0;
        this.unit = null;
        this.disposable = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(Context context, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        super.onBindView(context, location, provider, picker, listAnimationEnabled, itemAnimationEnabled);

        this.themePicker = picker;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        params.height = themePicker.getHeaderHeight();
        container.setLayoutParams(params);
        container.setOnClickListener(v -> {
            if (themePicker != null) {
                themePicker.getWeatherView().onClick();
            }
        });

        int textColor = picker.getHeaderTextColor(context);
        temperature.setTextColor(textColor);
        weather.setTextColor(textColor);
        aqiOrWind.setTextColor(textColor);

        unit = SettingsOptionManager.getInstance(context).getTemperatureUnit();
        if (location.getWeather() != null) {
            temperatureCFrom = temperatureCTo;
            temperatureCTo = location.getWeather().getCurrent().getTemperature().getTemperature();

            temperature.setEnableAnim(itemAnimationEnabled);
            temperature.setDuration(
                    (long) Math.min(
                            2000,
                            Math.max(temperatureCFrom, Math.abs(temperatureCTo) / 10f * 1000)
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

    @NotNull
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
                String.valueOf(unit.getTemperature(temperatureCFrom)),
                String.valueOf(unit.getTemperature(temperatureCTo))
        );
    }

    @Override
    public void onRecycleView() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    public int getCurrentTemperatureHeight() {
        return container.getMeasuredHeight() - temperature.getTop();
    }
}
