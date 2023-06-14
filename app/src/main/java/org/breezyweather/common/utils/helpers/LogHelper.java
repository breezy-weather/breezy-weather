package org.breezyweather.common.utils.helpers;

import android.util.Log;
import android.view.MotionEvent;

public class LogHelper {

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

    public static String nameAction(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";

            case MotionEvent.ACTION_POINTER_DOWN:
                return "ACTION_POINTER_DOWN";

            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";

            case MotionEvent.ACTION_POINTER_UP:
                return "ACTION_POINTER_UP";

            case MotionEvent.ACTION_UP:
                return "ACTION_UP";

            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
        }
        return "ACTION_UNKNOWN";
    }
}
