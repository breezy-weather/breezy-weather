package wangdaye.com.geometricweather.location.services;

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
import wangdaye.com.geometricweather.location.utils.LocationException;
import wangdaye.com.geometricweather.common.utils.helpers.BuglyHelper;

/**
 * A map location service.
 * */
public class AMapLocationService extends LocationService {

    private LocationCallback mLocationCallback;

    private final NotificationManagerCompat mNotificationManager;

    private AMapLocationClient mAMAPClient;
    private final AMapLocationListener mAMAPListener = new AMapLocationListener () {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            cancel();
            if (mLocationCallback != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    Result result = new Result(
                            (float) aMapLocation.getLatitude(),
                            (float) aMapLocation.getLongitude()
                    );
                    mLocationCallback.onCompleted(result);
                } else {
                    BuglyHelper.report(
                            new LocationException(
                                    aMapLocation.getErrorCode(),
                                    aMapLocation.getErrorInfo()
                            )
                    );
                    mLocationCallback.onCompleted(null);
                }
            }
        }
    };
    public AMapLocationService(Context context) {
        mNotificationManager = NotificationManagerCompat.from(context);
    }


    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback){
        mLocationCallback = callback;

        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        option.setOnceLocation(true);
        option.setOnceLocationLatest(true);
        option.setNeedAddress(true);
        option.setMockEnable(false);
        option.setLocationCacheEnable(false);
        mAMAPClient = new AMapLocationClient(context.getApplicationContext());
        mAMAPClient.setLocationOption(option);
        mAMAPClient.setLocationListener(mAMAPListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(getLocationNotificationChannel(context));
            mAMAPClient.enableBackgroundLocation(
                    GeometricWeather.NOTIFICATION_ID_LOCATION,
                    getLocationNotification(context));
        }
        mAMAPClient.startLocation();
    }

    @Override
    public void cancel() {
        if (mAMAPClient != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAMAPClient.disableBackgroundLocation(true);
            }
            mAMAPClient.stopLocation();
            mAMAPClient.onDestroy();
            mAMAPClient = null;
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