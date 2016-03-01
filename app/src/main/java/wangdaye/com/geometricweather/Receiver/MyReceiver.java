package wangdaye.com.geometricweather.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Service.NotificationService;

/**
 * My receiver.
 * */

public class MyReceiver extends BroadcastReceiver {
    // TAG
    private final String TAG = "MyReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                // power on the phone
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if(sharedPreferences.getBoolean(context.getString(R.string.key_notification_switch), false)) {
                    Intent intentNotification = new Intent(context, NotificationService.class);
                    context.startService(intentNotification);
                }
                break;
            case "android.intent.action.PACKAGE_ADDED":
                if(intent.getPackage().equals(context.getString(R.string.package_name))) {
                    Intent intentNotification = new Intent(context, NotificationService.class);
                    context.startService(intentNotification);
                }
                break;
        }
    }
}
