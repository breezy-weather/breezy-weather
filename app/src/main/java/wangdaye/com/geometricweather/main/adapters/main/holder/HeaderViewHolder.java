package wangdaye.com.geometricweather.main.adapters.main.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.ui.widgets.NumberAnimTextView;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.weatherView.WeatherView;

public class HeaderViewHolder extends AbstractMainViewHolder {

    private final LinearLayout mContainer;
    private final NumberAnimTextView mTemperature;
    private final TextView mWeather;
    private final TextView mAqiOrWind;

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
        mTemperature = itemView.findViewById(R.id.container_main_header_tempTxt);
        mWeather = itemView.findViewById(R.id.container_main_header_weatherTxt);
        mAqiOrWind = itemView.findViewById(R.id.container_main_header_aqiOrWindTxt);

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

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mContainer.getLayoutParams();
        params.height = ThemeManager
                .getInstance(context)
                .getWeatherThemeDelegate()
                .getHeaderHeight(context);
        mContainer.setLayoutParams(params);

        int textColor = ThemeManager
                .getInstance(context)
                .getWeatherThemeDelegate()
                .getHeaderTextColor(context);
        mTemperature.setTextColor(textColor);
        mWeather.setTextColor(textColor);
        mAqiOrWind.setTextColor(textColor);

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
                mTemperature.setPostfixString(mTemperatureUnit.getShortName(context));
            }

            StringBuilder title = new StringBuilder();
            if (!TextUtils.isEmpty(location.getWeather().getCurrent().getWeatherText())) {
                title.append(location.getWeather().getCurrent().getWeatherText());
            }
            if (location.getWeather().getCurrent().getTemperature().getFeelsLikeTemperature() != null) {
                if (!TextUtils.isEmpty(location.getWeather().getCurrent().getWeatherText())) {
                    title.append(", ");
                }

                title.append(context.getString(R.string.feels_like))
                        .append(" ")
                        .append(location.getWeather().getCurrent().getTemperature().getShortFeelsLikeTemperature(context, mTemperatureUnit));
            } else if (location.getWeather().getCurrent().getTemperature().getFeelsLikeTemperature() != null) {
                if (!TextUtils.isEmpty(location.getWeather().getCurrent().getWeatherText())) {
                    title.append(", ");
                }

                title.append(context.getString(R.string.feels_like))
                        .append(" ")
                        .append(location.getWeather().getCurrent().getTemperature().getShortFeelsLikeTemperature(context, mTemperatureUnit));
            }
            mWeather.setText(title.toString());

            if (location.getWeather().getCurrent().getAirQuality() != null && !TextUtils.isEmpty(location.getWeather().getCurrent().getAirQuality().getName(context, null))) {
                mAqiOrWind.setText(
                        context.getString(R.string.air_quality)
                                + " - "
                                + location.getWeather().getCurrent().getAirQuality().getName(context, null)
                );
            } else if (location.getWeather().getCurrent().getWind() != null && !TextUtils.isEmpty(location.getWeather().getCurrent().getWind().getShortWindDescription())) {
                mAqiOrWind.setText(
                        context.getString(R.string.wind)
                                + " - "
                                + location.getWeather().getCurrent().getWind().getShortWindDescription()
                );
            }

            itemView.setContentDescription(location.getCityName(context)
                    + ", " + location.getWeather().getCurrent().getTemperature().getTemperature(context, mTemperatureUnit)
                    + ", " + mWeather.getText()
                    + ", " + mAqiOrWind.getText());
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
