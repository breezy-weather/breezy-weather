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
import androidx.annotation.WorkerThread;

import android.text.TextUtils;

import java.io.IOException;
import java.util.List;

import wangdaye.com.geometricweather.utils.manager.ThreadManager;

/**
 * Android Location service.
 * */

@SuppressLint("MissingPermission")
public class AndroidLocationService extends LocationService {

    private Context context;
    private Handler delayedExecutor;
    private Handler poster;

    @Nullable private LocationManager locationManager;
    @Nullable private LocationListener locationListener;
    @Nullable private LocationCallback locationCallback;

    private long workingFlag;

    private static final long NULL_WORKING_FLAG = -1;
    private static final long POLLING_INTERVAL = 300;

    private static final long TIMEOUT_NETWORK = 10000;
    private static final long TIMEOUT_GPS = 60000;

    private class LocationListener implements android.location.LocationListener {

        private boolean geocode;

        LocationListener(boolean geocode) {
            this.geocode = geocode;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                handleLocation(location, geocode);
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
        context = c;
        delayedExecutor = new Handler(Looper.getMainLooper());
        poster = new Handler(Looper.getMainLooper());

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = null;
        locationCallback = null;

        workingFlag = NULL_WORKING_FLAG;
    }

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback, boolean geocode) {
        stop();

        String provider = getProvider();
        if (locationManager == null
                || !hasPermissions(context)
                || TextUtils.isEmpty(provider)
                || !locationManager.getAllProviders().contains(provider)) {
            dispatchResult(callback, null);
            return;
        }

        long flag = System.currentTimeMillis();
        workingFlag = flag;

        locationCallback = callback;

        Location latest = locationManager.getLastKnownLocation(provider);
        if (latest != null) {
            handleLocation(latest, geocode);
            return;
        }

        locationListener = new LocationListener(geocode);
        locationManager.requestSingleUpdate(provider, locationListener, Looper.getMainLooper());

        ThreadManager.getInstance().execute(() -> {
            Location l;
            while (workingFlag == flag) {
                l = locationManager.getLastKnownLocation(provider);
                if (l != null) {
                    handleLocation(l, geocode);
                    return;
                }

                try {
                    Thread.sleep(POLLING_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        delayedExecutor.postDelayed(this::stop, getTimeOut(provider));
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
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }

        LocationCallback c = locationCallback;
        if (locationCallback != null) {
            locationCallback = null;
            dispatchResult(c, null);
        }

        delayedExecutor.removeCallbacksAndMessages(null);

        workingFlag = NULL_WORKING_FLAG;
    }

    private void handleLocation(@NonNull Location location, boolean geocode) {
        LocationCallback c = locationCallback;
        if (locationCallback != null) {
            locationCallback = null;
            ThreadManager.getInstance().execute(() ->
                    dispatchResult(c, buildResult(location, geocode))
            );
        }
        stop();
    }

    @WorkerThread
    @Nullable
    private Result buildResult(@NonNull Location location, boolean geocode) {
        if (!location.hasAccuracy()) {
            return null;
        }

        Result result = new Result(
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude())
        );
        result.hasGeocodeInformation = false;

        if (!hasValidGeocoder() || !geocode) {
            return result;
        }

        List<Address> addressList = null;
        try {
            addressList = new Geocoder(context).getFromLocation(
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

    @Nullable
    private String getProvider() {
        if (locationManager == null) {
            return null;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else {
            return null;
        }
    }

    private long getTimeOut(@Nullable String provider) {
        if (provider == null) {
            return 0;
        }

        switch (provider) {
            case LocationManager.NETWORK_PROVIDER:
                return TIMEOUT_NETWORK;

            case LocationManager.GPS_PROVIDER:
                return TIMEOUT_GPS;
        }

        return 0;
    }

    private void dispatchResult(@NonNull LocationCallback callback, @Nullable Result r) {
        poster.post(() -> callback.onCompleted(r));
    }

    public static boolean hasValidGeocoder() {
        return Geocoder.isPresent();
    }
}
