package wangdaye.com.geometricweather.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import wangdaye.com.geometricweather.Service.WidgetService;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class WidgetReceiver extends BroadcastReceiver {
    // TAG
    private final String TAG = "WidgetReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentWidget = new Intent(context, WidgetService.class);
        context.startService(intentWidget);
    }
}