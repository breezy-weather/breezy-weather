package wangdaye.com.geometricweather.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import wangdaye.com.geometricweather.Service.NotificationService;

/**
 * Start the notification service after it wake this receiver up.
 * */

public class NotificationReceiver extends BroadcastReceiver {
    // TAG
//    private final static String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentNotification = new Intent(context, NotificationService.class);
        context.startService(intentNotification);
    }
}