package wangdaye.com.geometricweather.resource.provider;

import android.animation.Animator;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.content.res.ResourcesCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.resource.Constants;
import wangdaye.com.geometricweather.ui.image.pixel.PixelMoonDrawable;
import wangdaye.com.geometricweather.ui.image.pixel.PixelSunDrawable;

public class PixelResourcesProvider extends IconPackResourcesProvider {

    PixelResourcesProvider(@NonNull ResourceProvider defaultProvider) {
        super(
                GeometricWeather.getInstance(),
                GeometricWeather.getInstance().getPackageName(),
                defaultProvider
        );
    }

    static boolean isPixelIconProvider(@NonNull String packageName) {
        return packageName.equals(
                GeometricWeather.getInstance().getPackageName() + ".Pixel"
        );
    }

    @Override
    public String getPackageName() {
        return super.getPackageName() + ".Pixel";
    }

    @Override
    public String getProviderName() {
        return "Pixel";
    }

    @Override
    public Drawable getProviderIcon() {
        return ResourcesCompat.getDrawable(
                GeometricWeather.getInstance().getResources(),
                R.drawable.weather_partly_cloudy_day_pixel,
                null
        );
    }

    // weather icon.

    @Override
    @Size(3)
    public Drawable[] getWeatherIcons(String weatherKind, boolean dayTime) {
        return new Drawable[] {getWeatherIcon(weatherKind, dayTime), null, null};
    }

    @Override
    String getWeatherIconName(String weatherKind, boolean daytime) {
        return super.getWeatherIconName(weatherKind, daytime) + Constants.SEPARATOR + "pixel";
    }

    @Override
    String getWeatherIconName(String weatherKind, boolean daytime,
                                     @IntRange(from = 1, to = 3) int index) {
        if (index == 1) {
            return getWeatherIconName(weatherKind, daytime);
        } else {
            return null;
        }
    }

    // animator.

    @Override
    @Size(3)
    public Animator[] getWeatherAnimators(String weatherKind, boolean dayTime) {
        return new Animator[] {null, null, null};
    }

    @Override
    String getWeatherAnimatorName(String weatherKind, boolean daytime,
                                  @IntRange(from = 1, to = 3) int index) {
        return null;
    }

    // sun and moon.

    @NonNull
    public Drawable getSunDrawable() {
        return new PixelSunDrawable();
    }

    @NonNull
    public Drawable getMoonDrawable() {
        return new PixelMoonDrawable();
    }

    @Override
    @Nullable
    String getSunDrawableClassName() {
        return PixelSunDrawable.class.toString();
    }

    @Override
    @Nullable
    String getMoonDrawableClassName() {
        return PixelMoonDrawable.class.toString();
    }
}
