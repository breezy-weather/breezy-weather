package org.breezyweather.main.adapters.main.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.divider.MaterialDivider;
import org.breezyweather.common.basic.models.options.unit.RelativeHumidityUnit;
import org.breezyweather.common.basic.models.options.unit.SpeedUnit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.ui.widgets.NumberAnimTextView;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.theme.weatherView.WeatherView;
import org.breezyweather.R;
import org.breezyweather.settings.SettingsManager;

public class HeaderViewHolder extends AbstractMainViewHolder {

    private final LinearLayout mContainer;
    private final NumberAnimTextView mTemperature;
    private final TextView mTemperatureUnitView;
    private final TextView mWeatherText;
    private final TextView mFeelsLikeLabel;
    private final TextView mFeelsLike;
    private final MaterialDivider mFeelsLikeDivider;
    private final TextView mWindLabel;
    private final TextView mWind;
    private final MaterialDivider mWindDivider;
    private final TextView mUvLabel;
    private final TextView mUv;
    private final MaterialDivider mUvDivider;
    private final TextView mHumidityLabel;
    private final TextView mHumidity;

    private int mTemperatureCFrom;
    private int mTemperatureCTo;
    private TemperatureUnit mTemperatureUnit;
    private @Nullable Disposable mDisposable;

    public HeaderViewHolder(ViewGroup parent, WeatherView weatherView) {
        super(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.container_main_header, parent, false)
        );

        mContainer = itemView.findViewById(R.id.container_main_header);
        mTemperature = itemView.findViewById(R.id.container_main_header_temperature_value);
        mTemperatureUnitView = itemView.findViewById(R.id.container_main_header_temperature_unit);
        mWeatherText = itemView.findViewById(R.id.container_main_header_weather_text);
        mFeelsLikeLabel = itemView.findViewById(R.id.container_main_header_feelsLike_label);
        mFeelsLike = itemView.findViewById(R.id.container_main_header_feelsLike_text);
        mFeelsLikeDivider = itemView.findViewById(R.id.container_main_header_feelsLike_divider);
        mWindLabel = itemView.findViewById(R.id.container_main_header_wind_label);
        mWind = itemView.findViewById(R.id.container_main_header_wind_text);
        mWindDivider = itemView.findViewById(R.id.container_main_header_wind_divider);
        mUvLabel = itemView.findViewById(R.id.container_main_header_uv_label);
        mUv = itemView.findViewById(R.id.container_main_header_uv_text);
        mUvDivider = itemView.findViewById(R.id.container_main_header_uv_divider);
        mHumidityLabel = itemView.findViewById(R.id.container_main_header_humidity_label);
        mHumidity = itemView.findViewById(R.id.container_main_header_humidity_text);

        mTemperatureCFrom = 0;
        mTemperatureCTo = 0;
        mTemperatureUnit = null;
        mDisposable = null;

        mContainer.setOnClickListener(v -> weatherView.onClick());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(Context context, @NonNull Location location, @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled);

        /*ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mContainer.getLayoutParams();
        params.height = ThemeManager
                .getInstance(context)
                .getWeatherThemeDelegate()
                .getHeaderHeight(context);
        mContainer.setLayoutParams(params);*/

        int textColor = ThemeManager
                .getInstance(context)
                .getWeatherThemeDelegate()
                .getHeaderTextColor(context);
        mTemperature.setTextColor(textColor);
        mTemperatureUnitView.setTextColor(textColor);
        mWeatherText.setTextColor(textColor);
        mFeelsLikeLabel.setTextColor(textColor);
        mFeelsLike.setTextColor(textColor);
        mFeelsLikeDivider.setDividerColor(textColor);
        mWindLabel.setTextColor(textColor);
        mWind.setTextColor(textColor);
        mWindDivider.setDividerColor(textColor);
        mUvLabel.setTextColor(textColor);
        mUv.setTextColor(textColor);
        mUvDivider.setDividerColor(textColor);
        mHumidityLabel.setTextColor(textColor);
        mHumidity.setTextColor(textColor);

        mTemperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit();
        if (location.getWeather() != null && location.getWeather().getCurrent() != null) {
            if (location.getWeather().getCurrent().getTemperature() != null && location.getWeather().getCurrent().getTemperature().getTemperature() != null) {
                mTemperatureCFrom = mTemperatureCTo;
                mTemperatureCTo = location.getWeather().getCurrent().getTemperature().getTemperature();

                mTemperature.setEnableAnim(itemAnimationEnabled);
                mTemperature.setDuration(
                        (long) Math.min(
                                2000, // no longer than 2 seconds.
                                Math.abs(mTemperatureCTo - mTemperatureCFrom) / 10f * 1000
                        )
                );
                mTemperatureUnitView.setText(mTemperatureUnit.getShortName(context));
            }

            if (!TextUtils.isEmpty(location.getWeather().getCurrent().getWeatherText())) {
                mWeatherText.setVisibility(View.VISIBLE);
                mWeatherText.setText(location.getWeather().getCurrent().getWeatherText());
            } else {
                mWeatherText.setVisibility(View.GONE);
            }

            // Feels Like
            boolean validFeelsLike = location.getWeather().getCurrent().getTemperature().getFeelsLikeTemperature() != null;
            if (validFeelsLike) {
                mFeelsLikeLabel.setVisibility(View.VISIBLE);
                mFeelsLike.setVisibility(View.VISIBLE);
                mFeelsLikeLabel.setText(context.getString(R.string.temperature_feels_like));
                mFeelsLike.setText(location.getWeather().getCurrent().getTemperature().getFeelsLikeTemperature(context, mTemperatureUnit));
            } else {
                mFeelsLikeLabel.setVisibility(View.GONE);
                mFeelsLike.setVisibility(View.GONE);
            }

            // Feels Like divider
            SpeedUnit speedUnit = SettingsManager.getInstance(context).getSpeedUnit();
            boolean validWind = location.getWeather().getCurrent().getWind() != null
                    && !TextUtils.isEmpty(location.getWeather().getCurrent().getWind().getShortWindDescription(context, speedUnit));
            if (validWind && validFeelsLike) {
                mFeelsLikeDivider.setVisibility(View.VISIBLE);
            } else {
                mFeelsLikeDivider.setVisibility(View.GONE);
            }

            // Wind
            if (validWind) {
                mWindLabel.setVisibility(View.VISIBLE);
                mWind.setVisibility(View.VISIBLE);
                mWindLabel.setText(context.getString(R.string.wind));
                mWind.setText(location.getWeather().getCurrent().getWind().getShortWindDescription(context, speedUnit));
            } else {
                mWindLabel.setVisibility(View.GONE);
                mWind.setVisibility(View.GONE);
            }

            // Wind divider
            boolean validUv = location.getWeather().getCurrent().getUV() != null
                    && location.isDaylight() // Don’t show UV at night
                    && location.getWeather().getCurrent().getUV().getIndex() != null;
            if (validUv && (validFeelsLike || validWind)) {
                mWindDivider.setVisibility(View.VISIBLE);
            } else {
                mWindDivider.setVisibility(View.GONE);
            }

            // UV
            if (validUv) {
                mUvLabel.setVisibility(View.VISIBLE);
                mUv.setVisibility(View.VISIBLE);
                mUvLabel.setText(context.getString(R.string.uv_index));
                mUv.setText(location.getWeather().getCurrent().getUV().getShortUVDescription());
            } else {
                mUvLabel.setVisibility(View.GONE);
                mUv.setVisibility(View.GONE);
            }

            // UV / Humidity divider
            boolean validHumidity = location.getWeather().getCurrent().getRelativeHumidity() != null;
            if (validHumidity && (validFeelsLike || validWind || validUv)) {
                mUvDivider.setVisibility(View.VISIBLE);
            } else {
                mUvDivider.setVisibility(View.GONE);
            }

            // Humidity
            if (validHumidity) {
                mHumidityLabel.setVisibility(View.VISIBLE);
                mHumidity.setVisibility(View.VISIBLE);
                mHumidityLabel.setText(context.getString(R.string.humidity));
                mHumidity.setText(RelativeHumidityUnit.PERCENT.getValueText(
                        context,
                        (int) (float) location.getWeather().getCurrent().getRelativeHumidity()
                ));
            } else {
                mHumidityLabel.setVisibility(View.GONE);
                mHumidity.setVisibility(View.GONE);
            }

            // FIXME: contains values of previous location if visibility = GONE and label is missing
            itemView.setContentDescription(location.getCityName(context)
                    + ", " + location.getWeather().getCurrent().getTemperature().getTemperature(context, mTemperatureUnit)
                    + ", " + mWeatherText.getText()
                    + ", " + mWind.getText()
                    + ", " + mUv.getText()
                    + ", " + mHumidity.getText());
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

    @SuppressLint("DefaultLocale")
    @Override
    public void onEnterScreen() {
        super.onEnterScreen();
        mTemperature.setNumberString(
                String.format("%d", mTemperatureUnit.getValueWithoutUnit(mTemperatureCFrom)),
                String.format("%d", mTemperatureUnit.getValueWithoutUnit(mTemperatureCTo))
        );
    }

    @Override
    public void onRecycleView() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
    }

    public int getCurrentTemperatureHeight() {
        return mContainer.getMeasuredHeight() - mTemperature.getTop();
    }
}
