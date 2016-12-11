package wangdaye.com.geometricweather.utils.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.view.activity.MainActivity;

/**
 * Shortcuts utils.
 * */

public class ShortcutsManager {
    // data
    private static final String PREFERENCE_NAME = "geometricweather_shortcuts_manager";
    private static final String KEY_HAS_PUBLISHED = "has_published";

    @RequiresApi(api = 25)
    public static void checkAndPublishShortcuts(Context c, List<Location> locationList) {
        if (needRefresh(c)) {
            refreshShortcuts(c, locationList);
        }
    }

    private static boolean needRefresh(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return !sharedPreferences.getBoolean(KEY_HAS_PUBLISHED, false);
    }

    @TargetApi(25)
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static void refreshShortcuts(Context c, List<Location> locationList) {
        ShortcutManager shortcutManager = c.getSystemService(ShortcutManager.class);

        List<ShortcutInfo> shortcutList = new ArrayList<>();
        for (int i = 0; i < locationList.size() && i < 5; i ++) {
            shortcutList.add(
                    new ShortcutInfo.Builder(c, locationList.get(i).name)
                            .setIcon(Icon.createWithResource(c, R.drawable.ic_shortcut))
                            .setShortLabel(locationList.get(i).name)
                            .setLongLabel(locationList.get(i).name)
                            .setIntent(
                                    new Intent("com.wangdaye.geometricweather.Main")
                                            .putExtra(MainActivity.KEY_CITY, locationList.get(i).name))
                            .build());
        }

        shortcutManager.setDynamicShortcuts(shortcutList);
    }
}
