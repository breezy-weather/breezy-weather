package wangdaye.com.geometricweather.main;

import android.content.Context;

import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

/**
 * Main list decoration.
 * */

public class MainListDecoration extends ListDecoration {

    public MainListDecoration(Context context) {
        super(
                context,
                ThemeManager.getInstance(context).getLineColor(context)
        );
    }
}
