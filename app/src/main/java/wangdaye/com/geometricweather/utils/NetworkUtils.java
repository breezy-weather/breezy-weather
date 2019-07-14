package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkUtils {

    public static boolean isAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            return manager.getActiveNetworkInfo() != null;
        }
        return false;
    }
}