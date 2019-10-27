package wangdaye.com.geometricweather.settings.activity;

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

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.DefaultResourceProvider;
import wangdaye.com.geometricweather.resource.provider.PixelResourcesProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.adapter.WeatherIconAdapter;
import wangdaye.com.geometricweather.settings.dialog.AdaptiveIconDialog;
import wangdaye.com.geometricweather.settings.dialog.AnimatableIconDialog;
import wangdaye.com.geometricweather.settings.dialog.MinimalIconDialog;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

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
        itemList.add(new WeatherIcon(provider, WeatherCode.CLEAR, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.PARTLY_CLOUDY, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.CLOUDY, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.WIND, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.RAIN, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.SNOW, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.SLEET, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.HAIL, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.THUNDER, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.THUNDERSTORM, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.FOG, true));
        itemList.add(new WeatherIcon(provider, WeatherCode.HAZE, true));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title(getString(R.string.nighttime)));
        itemList.add(new WeatherIcon(provider, WeatherCode.CLEAR, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.PARTLY_CLOUDY, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.CLOUDY, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.WIND, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.RAIN, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.SNOW, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.SLEET, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.HAIL, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.THUNDER, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.THUNDERSTORM, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.FOG, false));
        itemList.add(new WeatherIcon(provider, WeatherCode.HAZE, false));
        itemList.add(new WeatherIconAdapter.Line());

        boolean darkMode = DisplayUtils.isDarkMode(this);

        itemList.add(new WeatherIconAdapter.Title("Minimal " + getString(R.string.daytime)));
        itemList.add(new MinimalIcon(provider, WeatherCode.CLEAR, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.PARTLY_CLOUDY, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.CLOUDY, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.WIND, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.RAIN, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.SNOW, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.SLEET, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.HAIL, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.THUNDER, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.THUNDERSTORM, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.FOG, true, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.HAZE, true, darkMode));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Minimal " + getString(R.string.nighttime)));
        itemList.add(new MinimalIcon(provider, WeatherCode.CLEAR, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.PARTLY_CLOUDY, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.CLOUDY, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.WIND, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.RAIN, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.SNOW, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.SLEET, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.HAIL, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.THUNDER, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.THUNDERSTORM, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.FOG, false, darkMode));
        itemList.add(new MinimalIcon(provider, WeatherCode.HAZE, false, darkMode));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Shortcuts " + getString(R.string.daytime)));
        itemList.add(new ShortcutIcon(provider, WeatherCode.CLEAR, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.PARTLY_CLOUDY, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.CLOUDY, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.WIND, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.RAIN, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.SNOW, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.SLEET, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.HAIL, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.THUNDER, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.THUNDERSTORM, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.FOG, true));
        itemList.add(new ShortcutIcon(provider, WeatherCode.HAZE, true));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Shortcuts " + getString(R.string.nighttime)));
        itemList.add(new ShortcutIcon(provider, WeatherCode.CLEAR, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.PARTLY_CLOUDY, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.CLOUDY, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.WIND, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.RAIN, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.SNOW, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.SLEET, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.HAIL, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.THUNDER, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.THUNDERSTORM, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.FOG, false));
        itemList.add(new ShortcutIcon(provider, WeatherCode.HAZE, false));
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
        recyclerView.setAdapter(new WeatherIconAdapter(this, itemList));
    }
}

abstract class BaseWeatherIcon extends WeatherIconAdapter.WeatherIcon {

    protected ResourceProvider provider;
    protected WeatherCode weatherCode;
    protected boolean daytime;

    BaseWeatherIcon(ResourceProvider provider, WeatherCode weatherCode, boolean daytime) {
        this.provider = provider;
        this.weatherCode = weatherCode;
        this.daytime = daytime;
    }
}

class WeatherIcon extends BaseWeatherIcon {

    WeatherIcon(ResourceProvider provider, WeatherCode weatherCode, boolean daytime) {
        super(provider, weatherCode, daytime);
    }

    @Override
    public Drawable getDrawable() {
        return ResourceHelper.getWeatherIcon(provider, weatherCode, daytime);
    }

    @Override
    public void onItemClicked(GeoActivity activity) {
        AnimatableIconDialog dialog = new AnimatableIconDialog();
        dialog.setData(
                weatherCode + "_" + (daytime ? "DAY" : "NIGHT"),
                ResourceHelper.getWeatherIcons(provider, weatherCode, daytime),
                ResourceHelper.getWeatherAnimators(provider, weatherCode, daytime)
        );
        dialog.show(activity.getSupportFragmentManager(), null);
    }
}

class MinimalIcon extends BaseWeatherIcon {

    private boolean darkMode;

    MinimalIcon(ResourceProvider provider, WeatherCode weatherCode, boolean daytime, boolean darkMode) {
        super(provider, weatherCode, daytime);
        this.darkMode = darkMode;
    }

    @Override
    public Drawable getDrawable() {
        return ResourceHelper.getWidgetNotificationIcon(
                provider, weatherCode, daytime, true, !darkMode);
    }

    @Override
    public void onItemClicked(GeoActivity activity) {
        MinimalIconDialog dialog = new MinimalIconDialog();
        dialog.setData(
                weatherCode + "_" + (daytime ? "DAY" : "NIGHT"),
                ResourceHelper.getMinimalXmlIcon(provider, weatherCode, daytime),
                ResourceHelper.getWidgetNotificationIcon(
                        provider, weatherCode, daytime, true, "light"),
                ResourceHelper.getWidgetNotificationIcon(
                        provider, weatherCode, daytime, true, "grey"),
                ResourceHelper.getWidgetNotificationIcon(
                        provider, weatherCode, daytime, true, "dark")
        );
        dialog.show(activity.getSupportFragmentManager(), null);
    }
}

class ShortcutIcon extends BaseWeatherIcon {

    ShortcutIcon(ResourceProvider provider, WeatherCode weatherCode, boolean daytime) {
        super(provider, weatherCode, daytime);
    }

    @Override
    public Drawable getDrawable() {
        return ResourceHelper.getShortcutsIcon(provider, weatherCode, daytime);
    }

    @Override
    public void onItemClicked(GeoActivity activity) {
        AdaptiveIconDialog dialog = new AdaptiveIconDialog();
        dialog.setData(
                weatherCode + "_" + (daytime ? "DAY" : "NIGHT"),
                ResourceHelper.getShortcutsForegroundIcon(provider, weatherCode, daytime),
                new ColorDrawable(Color.TRANSPARENT)
        );
        dialog.show(activity.getSupportFragmentManager(), null);
    }
}

class SunIcon extends WeatherIconAdapter.WeatherIcon {

    protected ResourceProvider provider;
    
    SunIcon(ResourceProvider provider) {
        this.provider = provider;
    }

    @Override
    public Drawable getDrawable() {
        return ResourceHelper.getSunDrawable(provider);
    }

    @Override
    public void onItemClicked(GeoActivity activity) {
        // do nothing.
    }
}

class MoonIcon extends SunIcon {

    MoonIcon(ResourceProvider provider) {
        super(provider);
    }

    @Override
    public Drawable getDrawable() {
        return ResourceHelper.getMoonDrawable(provider);
    }
}