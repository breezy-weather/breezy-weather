package wangdaye.com.geometricweather.background.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Main receiver.
 * */

public class MainReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            IntentHelper.startAwakeForegroundUpdateService(context);
        }
    }
}