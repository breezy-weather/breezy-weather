package wangdaye.com.geometricweather.utils;

import android.util.Log;

public class LogUtils {

    private static final boolean DEBUG = true;

    private static final String TAG = "testing";

    public static void log(String msg) {
        log(TAG, msg);
    }

    public static void log(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }
}
