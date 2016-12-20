package wangdaye.com.geometricweather.utils;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Permission utils.
 * */

public class PermissionUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestLocationPermission(Activity a, int code) {
        a.requestPermissions(
                new String[] {
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                code);
    }
}
