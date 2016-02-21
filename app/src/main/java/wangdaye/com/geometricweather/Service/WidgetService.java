package wangdaye.com.geometricweather.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class WidgetService extends Service {
    // TAG
    private final String TAG = "WidgetService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public  int onStartCommand(Intent intent, int flags, int startId) {
        Intent intentBroadcast = new Intent("com.geometricweather.receiver.REFRESH_WIDGET");
        sendBroadcast(intentBroadcast);

        this.stopSelf(startId);
        return START_NOT_STICKY;
    }
}