package wangdaye.com.geometricweather.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

public class NetworkUtils {
    private static Boolean isNetworkConnected = false;

    public static void registerNetworkCallback(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                                                                       @Override
                                                                       public void onAvailable(Network network) {
                                                                           NetworkUtils.isNetworkConnected = true;
                                                                       }

                                                                       @Override
                                                                       public void onLost(Network network) {
                                                                           NetworkUtils.isNetworkConnected = false;
                                                                       }
                                                                   }
                );
                NetworkUtils.isNetworkConnected = false;
            } catch (Exception e) {
                NetworkUtils.isNetworkConnected = false;
            }
        }
    }

    public static boolean isAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return NetworkUtils.isNetworkConnected;
        } else {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnected();
            }
            return false;
        }
    }
}