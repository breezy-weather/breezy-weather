package wangdaye.com.geometricweather.location.service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.utils.manager.ThreadManager;

/**
 * Android Location service.
 * */

@SuppressLint("MissingPermission")
public class AndroidLocationService extends LocationService {

    private Context context;
    private Handler handler;

    private LocationManager locationManager;
    private String provider;

    private Location location;
    private boolean working;
    private long time;
    private long timeOut;

    private List<LocationCallback> callbackList;

    private static final long UPDATE_INTERVAL = 1000;
    private static final long TIME_OUT_NETWORK = 10000;
    private static final long TIME_OUT_GPS = 60000;

    private android.location.LocationListener listener = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                AndroidLocationService.this.location = location;
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
            AndroidLocationService.this.provider = null;
        }
    };

    public AndroidLocationService(Context c) {
        context = c;
        handler = new Handler(Looper.getMainLooper());

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        provider = null;

        location = null;
        working = false;
        time = timeOut = 0;

        callbackList = new ArrayList<>();
    }

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback) {
        callbackList.add(callback);

        if (working) {
            return;
        } else {
            working = true;
        }

        ThreadManager.getInstance().execute(() -> {
            if (locationManager == null || !hasPermissions(context) || !bindProvider()) {
                // can not get location manager.
                // or can not get permissions.
                // or can not bind a valid location provider.
                // request location update failed.
                dispatchResult(null);
                stop();
                return;
            }

            postLocationUpdateRequestToMainThread();

            while (working) {
                if (updateLocation()) {
                    // call onCompleted(Result r) in updateLocation().
                    stop();
                } else {
                    try {
                        Thread.sleep(UPDATE_INTERVAL);
                    } catch (Exception ignored) {
                        // do nothing.
                    }
                    if (TextUtils.isEmpty(provider)
                            || !locationManager.getAllProviders().contains(provider)) {
                        // provider is not valid.
                        if (bindProvider()) {
                            postLocationUpdateRequestToMainThread();
                        }
                    }

                    time += UPDATE_INTERVAL;
                    if (time >= timeOut) {
                        stop();
                        dispatchResult(null);
                    }
                }
            }
        });
    }

    @Override
    public void cancel() {
        stop();
    }

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    private void stop() {
        working = false;
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }

    private boolean updateLocation() {
        if (!TextUtils.isEmpty(provider)
                && locationManager.getAllProviders().contains(provider)) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l != null) {
                location = l;
            }
            Result result = buildResult();
            if (result != null) {
                dispatchResult(result);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private Result buildResult() {
        if (location != null && location.hasAccuracy()
                && Geocoder.isPresent()) {

            List<Address> addressList = null;
            try {
                addressList = new Geocoder(context).getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressList != null && addressList.size() > 0) {
                Result result = new Result();

                result.district = addressList.get(0).getSubLocality();
                result.city = addressList.get(0).getLocality();
                if (TextUtils.isEmpty(result.city)) {
                    result.city = addressList.get(0).getSubAdminArea();
                }
                result.province = addressList.get(0).getAdminArea();
                result.country = addressList.get(0).getCountryName();

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

                result.latitude = String.valueOf(location.getLatitude());
                result.longitude = String.valueOf(location.getLongitude());
                return result;
            }
        }
        return null;
    }

    private boolean bindProvider() {
        if (locationManager == null) {
            provider = null;
            time = timeOut = 0;
            return false;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
            time = 0;
            timeOut = TIME_OUT_NETWORK;
            return true;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            time = 0;
            timeOut = TIME_OUT_GPS;
            return true;
        } else {
            provider = null;
            time = timeOut = 0;
            return false;
        }
    }

    private void postLocationUpdateRequestToMainThread() {
        handler.post(() -> {
            if (locationManager != null) {
                locationManager.removeUpdates(listener);
                locationManager.requestSingleUpdate(provider, listener, null);
            }
        });
    }

    private void dispatchResult(@Nullable Result r) {
        handler.post(() -> {
            for (int i = 0; i < callbackList.size(); i ++) {
                if (callbackList.get(i) != null) {
                    callbackList.get(i).onCompleted(r);
                }
            }
            callbackList.clear();
        });
    }
}
