package wangdaye.com.geometricweather.service;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import wangdaye.com.geometricweather.utils.helpter.TileHelper;
import wangdaye.com.geometricweather.view.activity.MainActivity;

/**
 * Geo tile service.
 * */

@RequiresApi(api = Build.VERSION_CODES.N)
public class GeoTileService extends TileService {

    /** <br> life cycle. */

    @Override
    public void onTileAdded() {
        TileHelper.setEnable(this, true);
        TileHelper.refreshTile(this, getQsTile());
    }

    @Override
    public void onTileRemoved() {
        TileHelper.setEnable(this, false);
    }

    @Override
    public void onStartListening () {
        TileHelper.refreshTile(this, getQsTile());
    }

    @Override
    public void onStopListening () {
        TileHelper.refreshTile(this, getQsTile());
    }

    @Override
    public void onClick () {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}