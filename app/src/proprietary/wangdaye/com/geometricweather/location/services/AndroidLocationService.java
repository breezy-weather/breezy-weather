package wangdaye.com.geometricweather.location.services;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Android Location service.
 * */

@SuppressLint("MissingPermission")
public class AndroidLocationService extends LocationService {

    private final Handler mTimer;

    @Nullable private LocationManager mLocationManager;
    @Nullable private FusedLocationProviderClient mGMSClient;

    @Nullable private LocationListener mNetworkListener;
    @Nullable private LocationListener mGPSListener;
    @Nullable private GMSLocationListener mGMSListener;

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

    private class GMSLocationListener extends com.google.android.gms.location.LocationCallback {

        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null && locationResult.getLocations().size() > 0) {
                stopLocationUpdates();
                handleLocation(locationResult.getLocations().get(0));
            }
        }
    }

    public AndroidLocationService() {
        mTimer = new Handler(Looper.getMainLooper());

        mNetworkListener = null;
        mGPSListener = null;
        mGMSListener = null;

        mLocationCallback = null;
        mLastKnownLocation = null;
    }

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback){
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mGMSClient = gmsEnabled(context)
                ? LocationServices.getFusedLocationProviderClient(context)
                : null;

        if (mLocationManager == null
                || !locationEnabled(context, mLocationManager)
                || !hasPermissions(context)) {
            callback.onCompleted(null);
            return;
        }

        mNetworkListener = new LocationListener();
        mGPSListener = new LocationListener();
        mGMSListener = new GMSLocationListener();

        mLocationCallback = callback;
        mLastKnownLocation = getLastKnownLocation();
        if (mLastKnownLocation == null && mGMSClient != null) {
            mGMSClient.getLastLocation()
                    .addOnSuccessListener(location -> mLastKnownLocation = location);
        }

        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, mNetworkListener, Looper.getMainLooper());
        }
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, mGPSListener, Looper.getMainLooper());
        }
        if (mGMSClient != null) {
            mGMSClient.requestLocationUpdates(
                    LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                            .setNumUpdates(1),
                    mGMSListener,
                    Looper.getMainLooper()
            );
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
        if (mGMSClient != null && mGMSListener != null) {
            mGMSClient.removeLocationUpdates(mGMSListener);
            mGMSListener = null;
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

        handleResultIfNecessary(buildResult(location));
    }

    private void handleResultIfNecessary(@Nullable Result result) {
        if (mLocationCallback != null) {
            mLocationCallback.onCompleted(result);
            mLocationCallback = null;
        }
    }

    @WorkerThread
    private Result buildResult(@NonNull Location location) {
        return new Result(
                (float) location.getLatitude(),
                (float) location.getLongitude()
        );
    }

    private static boolean gmsEnabled(Context context) {
        return GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
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
