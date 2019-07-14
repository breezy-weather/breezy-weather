package wangdaye.com.geometricweather.background.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.service.quicksettings.Tile;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Geo tile service.
 * */

@RequiresApi(api = Build.VERSION_CODES.N)
public class TileService extends android.service.quicksettings.TileService {

    @Override
    public void onTileAdded() {
        refreshTile(this, getQsTile());
    }

    @Override
    public void onTileRemoved() {
        // do nothing.
    }

    @Override
    public void onStartListening () {
        refreshTile(this, getQsTile());
    }

    @Override
    public void onStopListening () {
        refreshTile(this, getQsTile());
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onClick () {
        try {
            Object statusBarManager = getSystemService("statusbar");
            if (statusBarManager != null) {
                statusBarManager
                        .getClass()
                        .getMethod("collapsePanels")
                        .invoke(statusBarManager);
            }
        } catch (Exception ignored) {

        }
        IntentHelper.startMainActivity(this);
    }

    private static void refreshTile(Context context, Tile tile) {
        if (tile == null) {
            return;
        }
        Location location = DatabaseHelper.getInstance(context).readLocationList().get(0);
        location.weather = DatabaseHelper.getInstance(context).readWeather(location);
        if (location.weather != null) {
            boolean f = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.key_fahrenheit), false);
            tile.setIcon(
                    WeatherHelper.getMinimalIcon(
                            ResourcesProviderFactory.getNewInstance(),
                            location.weather.realTime.weatherKind,
                            TimeManager.getInstance(context).isDayTime()
                    )
            );
            tile.setLabel(
                    ValueUtils.buildCurrentTemp(
                            location.weather.realTime.temp, false, f
                    )
            );
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }
}