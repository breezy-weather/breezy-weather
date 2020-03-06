package wangdaye.com.geometricweather.main;

import android.content.Context;

import wangdaye.com.geometricweather.utils.DisplayUtils;

public class MainDisplayUtils {

    public static boolean isMultiFragmentEnabled(Context context) {
        return DisplayUtils.isTabletDevice(context) && DisplayUtils.isLandscape(context);
    }
}
