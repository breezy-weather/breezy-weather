package wangdaye.com.geometricweather.data.service.location;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;

import wangdaye.com.geometricweather.GeometricWeather;

/**
 * A map location service.
 * */

public class AMapLocationService extends LocationService {

    private AMapLocationClient client;
    private NotificationManager manager;
    private LocationCallback callback;

    private AMapLocationListener listener = new AMapLocationListener () {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            cancel();
            if (callback != null) {
                Log.d("location", "location code = " + aMapLocation.getErrorCode());
                switch (aMapLocation.getErrorCode()) {
                    case 0:
                        Result result = new Result();
                        result.district = aMapLocation.getDistrict();
                        result.city = aMapLocation.getCity();
                        result.province = aMapLocation.getProvince();
                        result.country = aMapLocation.getCountry();
                        result.latitude = String.valueOf(aMapLocation.getLatitude());
                        result.longitude = String.valueOf(aMapLocation.getLongitude());
                        result.inChina = CoordinateConverter.isAMapDataAvailable(
                                aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        callback.onCompleted(result);
                        break;

                    default:
                        callback.onCompleted(null);
                        break;
                }
            }
        }
    };

    public AMapLocationService(Context context) {
        manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
    }

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback){
        this.callback = callback;

        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        option.setOnceLocation(true);
        option.setOnceLocationLatest(true);
        option.setNeedAddress(true);
        option.setMockEnable(false);
        option.setLocationCacheEnable(false);
        client = new AMapLocationClient(context.getApplicationContext());
        client.setLocationOption(option);
        client.setLocationListener(listener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager != null) {
                manager.createNotificationChannel(getLocationNotificationChannel(context));
            }
            client.enableBackgroundLocation(
                    GeometricWeather.NOTIFICATION_ID_LOCATION,
                    getLocationNotification(context));
        }
        client.startLocation();
    }

    @Override
    public void cancel() {
        if (client != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
                client.disableBackgroundLocation(true);
            }
            client.stopLocation();
            client.onDestroy();
            client = null;
        }
    }
}