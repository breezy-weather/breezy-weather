package wangdaye.com.geometricweather.ui.activity;

import android.annotation.SuppressLint;
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
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.DefaultResourceProvider;
import wangdaye.com.geometricweather.resource.provider.PixelResourcesProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.ui.adapter.WeatherIconAdapter;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class PreviewIconActivity extends GeoActivity {

    private CoordinatorLayout container;

    private List<WeatherIconAdapter.Item> itemList;
    private ResourceProvider provider;

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
        itemList = new ArrayList<>();
        itemList.add(new WeatherIconAdapter.Title(getString(R.string.daytime)));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_CLEAR, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_PARTLY_CLOUDY, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_CLOUDY, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_RAIN, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_SNOW, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_WIND, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_FOG, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_HAZE, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_SLEET, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_HAIL, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_THUNDER, true));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_THUNDERSTORM, true));
        itemList.add(new WeatherIconAdapter.Line());
        itemList.add(new WeatherIconAdapter.Title(getString(R.string.nighttime)));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_CLEAR, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_PARTLY_CLOUDY, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_CLOUDY, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_RAIN, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_SNOW, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_WIND, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_FOG, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_HAZE, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_SLEET, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_HAIL, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_THUNDER, false));
        itemList.add(new WeatherIconAdapter.WeatherIcon(Weather.KIND_THUNDERSTORM, false));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Minimal " + getString(R.string.daytime)));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_CLEAR, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_PARTLY_CLOUDY, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_CLOUDY, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_RAIN, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_SNOW, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_WIND, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_FOG, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_HAZE, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_SLEET, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_HAIL, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_THUNDER, true));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_THUNDERSTORM, true));
        itemList.add(new WeatherIconAdapter.Line());
        itemList.add(new WeatherIconAdapter.Title("Minimal " + getString(R.string.nighttime)));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_CLEAR, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_PARTLY_CLOUDY, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_CLOUDY, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_RAIN, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_SNOW, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_WIND, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_FOG, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_HAZE, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_SLEET, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_HAIL, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_THUNDER, false));
        itemList.add(new WeatherIconAdapter.MinimalIcon(Weather.KIND_THUNDERSTORM, false));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title("Shortcuts " + getString(R.string.daytime)));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_CLEAR, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_PARTLY_CLOUDY, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_CLOUDY, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_RAIN, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_SNOW, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_WIND, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_FOG, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_HAZE, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_SLEET, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_HAIL, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_THUNDER, true));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_THUNDERSTORM, true));
        itemList.add(new WeatherIconAdapter.Line());
        itemList.add(new WeatherIconAdapter.Title("Shortcuts " + getString(R.string.nighttime)));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_CLEAR, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_PARTLY_CLOUDY, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_CLOUDY, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_RAIN, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_SNOW, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_WIND, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_FOG, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_HAZE, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_SLEET, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_HAIL, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_THUNDER, false));
        itemList.add(new WeatherIconAdapter.Shortcut(Weather.KIND_THUNDERSTORM, false));
        itemList.add(new WeatherIconAdapter.Line());

        itemList.add(new WeatherIconAdapter.Title(getString(R.string.sunrise_sunset)));
        itemList.add(new WeatherIconAdapter.SunIcon());
        itemList.add(new WeatherIconAdapter.MoonIcon());

        provider = ResourcesProviderFactory.getNewInstance(
                getIntent().getStringExtra(KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME)
        );
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
        recyclerView.setAdapter(new WeatherIconAdapter(this, itemList, provider));
    }
}