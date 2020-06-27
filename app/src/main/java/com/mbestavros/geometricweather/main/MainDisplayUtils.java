package com.mbestavros.geometricweather.main;

import android.content.Context;

import com.mbestavros.geometricweather.utils.DisplayUtils;

public class MainDisplayUtils {

    public static boolean isMultiFragmentEnabled(Context context) {
        return DisplayUtils.isTabletDevice(context) && DisplayUtils.isLandscape(context);
    }
}
