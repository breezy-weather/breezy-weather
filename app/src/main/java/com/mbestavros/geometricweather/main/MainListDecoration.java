package com.mbestavros.geometricweather.main;

import android.content.Context;

import com.mbestavros.geometricweather.ui.decotarion.ListDecoration;
import com.mbestavros.geometricweather.utils.manager.ThemeManager;

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
