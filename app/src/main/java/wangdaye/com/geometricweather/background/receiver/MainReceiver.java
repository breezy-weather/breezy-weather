package wangdaye.com.geometricweather.background.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Main receiver.
 * */

public class MainReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_WALLPAPER_CHANGED:
                IntentHelper.startAwakeForegroundUpdateService(context);
                break;
        }
    }
}