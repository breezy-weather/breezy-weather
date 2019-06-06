package wangdaye.com.geometricweather.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.DefaultResourceProvider;
import wangdaye.com.geometricweather.resource.provider.PixelResourcesProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.ui.adapter.WeatherIconAdapter;
import wangdaye.com.geometricweather.ui.dialog.AdaptiveIconDialog;
import wangdaye.com.geometricweather.ui.dialog.AnimatableIconDialog;
import wangdaye.com.geometricweather.ui.dialog.MinimalIconDialog;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class PreviewIconActivity extends GeoActivity {

    private CoordinatorLayout container;

    private ResourceProvider provider;
    private List<WeatherIconAdapter.Item> itemList;

    public static final String KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME
            = "ICON_PREVIEW_ACTIVITY_PACKAGE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_icon);
        initData();
        initWidget();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    private void initData() {
        provider = ResourcesProviderFactory.getNewInstance(
                getIntent().getStringExtra(KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME)
        );
        itemList = new ArrayList<>();

        itemList.add(new WeatherIconAdapter.Title(getString(R.string.daytime)));
        itemList.add(new WeatherIcon(provider, Weather.KIND_CLEAR, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_PARTLY_CLOUDY, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_CLOUDY, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_WIND, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_RAIN, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_SNOW, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_SLEET, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_HAIL, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_THUNDER, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_THUNDERSTORM, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_FOG, true));
        itemList.add(new WeatherIcon(provider, Weather.KIND_HAZE, true));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title(getString(R.string.nighttime)));
        itemList.add(new WeatherIcon(provider, Weather.KIND_CLEAR, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_PARTLY_CLOUDY, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_CLOUDY, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_WIND, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_RAIN, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_SNOW, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_SLEET, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_HAIL, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_THUNDER, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_THUNDERSTORM, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_FOG, false));
        itemList.add(new WeatherIcon(provider, Weather.KIND_HAZE, false));
        itemList.add(new WeatherIconAdapter.Line());

        boolean darkMode = DisplayUtils.isDarkMode(this);

        itemList.add(new WeatherIconAdapter.Title("Minimal " + getString(R.string.daytime)));
        itemList.add(new MinimalIcon(provider, Weather.KIND_CLEAR, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_PARTLY_CLOUDY, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_CLOUDY, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_WIND, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_RAIN, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_SNOW, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_SLEET, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_HAIL, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_THUNDER, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_THUNDERSTORM, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_FOG, true, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_HAZE, true, darkMode));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Minimal " + getString(R.string.nighttime)));
        itemList.add(new MinimalIcon(provider, Weather.KIND_CLEAR, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_PARTLY_CLOUDY, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_CLOUDY, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_WIND, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_RAIN, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_SNOW, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_SLEET, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_HAIL, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_THUNDER, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_THUNDERSTORM, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_FOG, false, darkMode));
        itemList.add(new MinimalIcon(provider, Weather.KIND_HAZE, false, darkMode));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Shortcuts " + getString(R.string.daytime)));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_CLEAR, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_PARTLY_CLOUDY, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_CLOUDY, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_WIND, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_RAIN, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_SNOW, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_SLEET, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_HAIL, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_THUNDER, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_THUNDERSTORM, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_FOG, true));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_HAZE, true));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Shortcuts " + getString(R.string.nighttime)));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_CLEAR, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_PARTLY_CLOUDY, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_CLOUDY, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_WIND, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_RAIN, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_SNOW, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_SLEET, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_HAIL, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_THUNDER, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_THUNDERSTORM, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_FOG, false));
        itemList.add(new ShortcutIcon(provider, Weather.KIND_HAZE, false));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title(getString(R.string.sunrise_sunset)));
        itemList.add(new SunIcon(provider));
        itemList.add(new MoonIcon(provider));
    }

    private void initWidget() {
        this.container = findViewById(R.id.activity_preview_icon_container);

        Toolbar toolbar = findViewById(R.id.activity_preview_icon_toolbar);
        toolbar.setTitle(provider.getProviderName());
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.activity_preview_icon);
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_appStore:
                    if (provider instanceof DefaultResourceProvider
                            || provider instanceof PixelResourcesProvider) {
                        IntentHelper.startAppStoreDetailsActivity(this);
                    } else {
                        IntentHelper.startAppStoreDetailsActivity(this, provider.getPackageName());
                    }
                    break;

                case R.id.action_about:
                    if (provider instanceof DefaultResourceProvider
                            || provider instanceof PixelResourcesProvider) {
                        IntentHelper.startApplicationDetailsActivity(this);
                    } else {
                        IntentHelper.startApplicationDetailsActivity(this, provider.getPackageName());
                    }
                    break;
            }
            return true;
        });

        RecyclerView recyclerView = findViewById(R.id.activity_preview_icon_recyclerView);
        GridLayoutManager manager = new GridLayoutManager(this, 4);
        manager.setSpanSizeLookup(WeatherIconAdapter.getSpanSizeLookup(4, itemList));
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new WeatherIconAdapter(itemList));
    }
}

abstract class BaseWeatherIcon extends WeatherIconAdapter.WeatherIcon {

    protected ResourceProvider provider;
    protected String weatherKind;
    protected boolean daytime;

    BaseWeatherIcon(ResourceProvider provider, String weatherKind, boolean daytime) {
        this.provider = provider;
        this.weatherKind = weatherKind;
        this.daytime = daytime;
    }
}

class WeatherIcon extends BaseWeatherIcon {

    WeatherIcon(ResourceProvider provider, String weatherKind, boolean daytime) {
        super(provider, weatherKind, daytime);
    }

    @Override
    public Drawable getDrawable() {
        return WeatherHelper.getWeatherIcon(provider, weatherKind, daytime);
    }

    @Override
    public void onItemClicked() {
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            AnimatableIconDialog dialog = new AnimatableIconDialog();
            dialog.setData(
                    weatherKind + "_" + (daytime ? "DAY" : "NIGHT"),
                    WeatherHelper.getWeatherIcons(provider, weatherKind, daytime),
                    WeatherHelper.getWeatherAnimators(provider, weatherKind, daytime)
            );
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }
}

class MinimalIcon extends BaseWeatherIcon {

    private boolean darkMode;

    MinimalIcon(ResourceProvider provider, String weatherKind, boolean daytime, boolean darkMode) {
        super(provider, weatherKind, daytime);
        this.darkMode = darkMode;
    }

    @Override
    public Drawable getDrawable() {
        return WeatherHelper.getWidgetNotificationIcon(
                provider, weatherKind, daytime, true, !darkMode);
    }

    @Override
    public void onItemClicked() {
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            MinimalIconDialog dialog = new MinimalIconDialog();
            dialog.setData(
                    weatherKind + "_" + (daytime ? "DAY" : "NIGHT"),
                    WeatherHelper.getMinimalXmlIcon(provider, weatherKind, daytime),
                    WeatherHelper.getWidgetNotificationIcon(
                            provider, weatherKind, daytime, true, "light"),
                    WeatherHelper.getWidgetNotificationIcon(
                            provider, weatherKind, daytime, true, "grey"),
                    WeatherHelper.getWidgetNotificationIcon(
                            provider, weatherKind, daytime, true, "dark")
            );
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }
}

class ShortcutIcon extends BaseWeatherIcon {

    ShortcutIcon(ResourceProvider provider, String weatherKind, boolean daytime) {
        super(provider, weatherKind, daytime);
    }

    @Override
    public Drawable getDrawable() {
        return WeatherHelper.getShortcutsIcon(provider, weatherKind, daytime);
    }

    @Override
    public void onItemClicked() {
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            AdaptiveIconDialog dialog = new AdaptiveIconDialog();
            dialog.setData(
                    weatherKind + "_" + (daytime ? "DAY" : "NIGHT"),
                    WeatherHelper.getShortcutsForegroundIcon(provider, weatherKind, daytime),
                    new ColorDrawable(Color.TRANSPARENT)
            );
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }
}

class SunIcon extends WeatherIconAdapter.WeatherIcon {

    protected ResourceProvider provider;
    
    SunIcon(ResourceProvider provider) {
        this.provider = provider;
    }

    @Override
    public Drawable getDrawable() {
        return WeatherHelper.getSunDrawable(provider);
    }

    @Override
    public void onItemClicked() {

    }
}

class MoonIcon extends SunIcon {

    MoonIcon(ResourceProvider provider) {
        super(provider);
    }

    @Override
    public Drawable getDrawable() {
        return WeatherHelper.getMoonDrawable(provider);
    }
}