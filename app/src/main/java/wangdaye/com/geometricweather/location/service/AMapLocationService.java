package wangdaye.com.geometricweather.location.service;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.location.LocationException;
import wangdaye.com.geometricweather.utils.helpter.BuglyHelper;

/**
 * A map location service.
 * */
public class AMapLocationService extends LocationService {

    private AMapLocationClient client;
    private NotificationManagerCompat manager;
    private LocationCallback callback;

    private AMapLocationListener listener = new AMapLocationListener () {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            cancel();
            if (callback != null) {
                switch (aMapLocation.getErrorCode()) {
                    case 0:
                        Result result = new Result(
                                (float) aMapLocation.getLatitude(),
                                (float) aMapLocation.getLongitude()
                        );
                        result.setGeocodeInformation(
                                aMapLocation.getCountry(),
                                aMapLocation.getProvince(),
                                aMapLocation.getCity(),
                                aMapLocation.getDistrict()
                        );
                        result.inChina = CoordinateConverter.isAMapDataAvailable(
                                aMapLocation.getLatitude(),
                                aMapLocation.getLongitude()
                        );
                        callback.onCompleted(result);
                        break;

                    default:
                        BuglyHelper.report(
                                new LocationException(
                                        aMapLocation.getErrorCode(),
                                        aMapLocation.getErrorInfo()
                                )
                        );
                        callback.onCompleted(null);
                        break;
                }
            }
        }
    };
    public AMapLocationService(Context context) {
        manager = NotificationManagerCompat.from(context);
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
            manager.createNotificationChannel(getLocationNotificationChannel(context));
            client.enableBackgroundLocation(
                    GeometricWeather.NOTIFICATION_ID_LOCATION,
                    getLocationNotification(context));
        }
        client.startLocation();
    }

    @Override
    public void cancel() {
        if (client != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                client.disableBackgroundLocation(true);
            }
            client.stopLocation();
            client.onDestroy();
            client = null;
        }
    }

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
}