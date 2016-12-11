package wangdaye.com.geometricweather.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

/**
 * Permission utils.
 * */

public class PermissionUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestLocationPermission(Activity a, int code, OnRequestPermissionCallback callback) {
        if (ContextCompat.checkSelfPermission(
                a, Manifest.permission.INSTALL_LOCATION_PROVIDER) != PackageManager.PERMISSION_GRANTED) {
            a.requestPermissions(new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, code);
        } else {
            callback.onRequestSuccess();
        }
    }

    public interface OnRequestPermissionCallback {
        void onRequestSuccess();
    }
}
