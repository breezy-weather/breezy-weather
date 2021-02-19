package wangdaye.com.geometricweather.location.services;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.IOException;
import java.util.List;

import wangdaye.com.geometricweather.common.utils.LanguageUtils;
import wangdaye.com.geometricweather.common.utils.helpters.AsyncHelper;

/**
 * Android Location service.
 * */

@SuppressLint("MissingPermission")
public class AndroidLocationService extends LocationService {

    private final Context mContext;
    private final Handler mTimer;

    @Nullable private LocationManager mLocationManager;

    @Nullable private LocationListener mNetworkListener;
    @Nullable private LocationListener mGPSListener;

    @Nullable private LocationCallback mLocationCallback;
    @Nullable private Location mLastKnownLocation;

    private static final long TIMEOUT_MILLIS = 10 * 1000;

    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                stopLocationUpdates();
                handleLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // do nothing.
        }

        @Override
        public void onProviderEnabled(String provider) {
            // do nothing.
        }

        @Override
        public void onProviderDisabled(String provider) {
            // do nothing.
        }
    }

    public AndroidLocationService(Context c) {
        mContext = c;
        mTimer = new Handler(Looper.getMainLooper());

        mNetworkListener = null;
        mGPSListener = null;

        mLocationCallback = null;
        mLastKnownLocation = null;
    }

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback){
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (mLocationManager == null
                || !locationEnabled(context, mLocationManager)
                || !hasPermissions(context)) {
            callback.onCompleted(null);
            return;
        }

        mNetworkListener = new LocationListener();
        mGPSListener = new LocationListener();

        mLocationCallback = callback;
        mLastKnownLocation = getLastKnownLocation();

        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, mNetworkListener, Looper.getMainLooper());
        }
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, mGPSListener, Looper.getMainLooper());
        }

        mTimer.postDelayed(() -> {
            stopLocationUpdates();
            handleLocation(mLastKnownLocation);
        }, TIMEOUT_MILLIS);
    }

    @Nullable
    private Location getLastKnownLocation() {
        if (mLocationManager == null) {
            return null;
        }

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            return location;
        }
        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            return location;
        }
        return mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    }

    @Override
    public void cancel() {
        stopLocationUpdates();
        mLocationCallback = null;
        mTimer.removeCallbacksAndMessages(null);
    }

    private void stopLocationUpdates() {
        if (mLocationManager != null) {
            if (mNetworkListener != null) {
                mLocationManager.removeUpdates(mNetworkListener);
                mNetworkListener = null;
            }
            if (mGPSListener != null) {
                mLocationManager.removeUpdates(mGPSListener);
                mGPSListener = null;
            }
        }
    }

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    private void handleLocation(@Nullable Location location) {
        if (location == null) {
            handleResultIfNecessary(null);
            return;
        }

        AsyncHelper.runOnIO(
                (AsyncHelper.Task<Result>) emitter -> emitter.send(buildResult(location), true),
                (result, done) -> handleResultIfNecessary(result)
        );
    }

    private void handleResultIfNecessary(@Nullable Result result) {
        if (mLocationCallback != null) {
            mLocationCallback.onCompleted(result);
            mLocationCallback = null;
        }
    }

    @WorkerThread
    private Result buildResult(@NonNull Location location) {
        Result result = new Result((float) location.getLatitude(), (float) location.getLongitude());
        result.hasGeocodeInformation = false;

        if (!Geocoder.isPresent()) {
            return result;
        }

        List<Address> addressList = null;
        try {
            addressList = new Geocoder(mContext, LanguageUtils.getCurrentLocale(mContext))
                    .getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList == null || addressList.size() == 0) {
            return result;
        }

        result.setGeocodeInformation(
                addressList.get(0).getCountryName(),
                addressList.get(0).getAdminArea(),
                TextUtils.isEmpty(addressList.get(0).getLocality())
                        ? addressList.get(0).getSubAdminArea()
                        : addressList.get(0).getLocality(),
                addressList.get(0).getSubLocality()
        );

        String countryCode = addressList.get(0).getCountryCode();
        if (TextUtils.isEmpty(countryCode)) {
            if (TextUtils.isEmpty(result.country)) {
                result.inChina = false;
            } else {
                result.inChina = result.country.equals("中国")
                        || result.country.equals("香港")
                        || result.country.equals("澳门")
                        || result.country.equals("台湾")
                        || result.country.equals("China");
            }
        } else {
            result.inChina = countryCode.equals("CN")
                    || countryCode.equals("cn")
                    || countryCode.equals("HK")
                    || countryCode.equals("hk")
                    || countryCode.equals("TW")
                    || countryCode.equals("tw");
        }

        return result;
    }

    private static boolean locationEnabled(Context context, @NonNull LocationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!manager.isLocationEnabled()) {
                return false;
            }
        } else {
            int locationMode = -1;
            try {
                locationMode = Settings.Secure.getInt(
                        context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                return false;
            }
        }

        return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
