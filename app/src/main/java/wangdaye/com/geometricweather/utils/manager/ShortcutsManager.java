package wangdaye.com.geometricweather.utils.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Shortcuts utils.
 * */

public class ShortcutsManager {

    @TargetApi(25)
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static void refreshShortcuts(Context c, List<Location> locationList) {
        ShortcutManager shortcutManager = c.getSystemService(ShortcutManager.class);

        List<ShortcutInfo> shortcutList = new ArrayList<>();
        String title;
        for (int i = 0; i < locationList.size(); i ++) {
            title = locationList.get(i).isLocal() ? c.getString(R.string.local) : locationList.get(i).city;
            shortcutList.add(
                    new ShortcutInfo.Builder(c, locationList.get(i).cityId)
                            .setIcon(Icon.createWithResource(c, R.drawable.ic_shortcut))
                            .setShortLabel(title)
                            .setLongLabel(title)
                            .setIntent(IntentHelper.buildMainActivityIntent(c, locationList.get(i)))
                            .build());
        }

        shortcutManager.setDynamicShortcuts(shortcutList);
    }
}
