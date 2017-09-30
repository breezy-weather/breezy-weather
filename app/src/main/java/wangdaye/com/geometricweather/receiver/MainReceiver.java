package wangdaye.com.geometricweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;

/**
 * Main receiver.
 * */

public class MainReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                try {
                    ServiceHelper.resetNormalService(context, false, true);
                    ServiceHelper.resetForecastService(context, true);
                    ServiceHelper.resetForecastService(context, false);
                } catch (Exception ignored) {

                }
                break;
        }
    }
}