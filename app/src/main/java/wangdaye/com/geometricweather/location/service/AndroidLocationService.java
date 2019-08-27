package wangdaye.com.geometricweather.location.service;


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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import android.provider.Settings;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Android Location service.
 * */

@SuppressLint("MissingPermission")
public class AndroidLocationService extends LocationService {

    private Context context;
    private Handler timer;

    @Nullable private LocationManager locationManager;
    @Nullable private FusedLocationProviderClient gmsLocationClient;

    @Nullable private LocationListener networkListener;
    @Nullable private LocationListener gpsListener;
    @Nullable private GMSLocationListener gmsListener;

    @Nullable private LocationCallback locationCallback;
    @Nullable private Location lastKnownLocation;

    private static final long TIMEOUT_MILLIS = 10 * 1000;

    private class LocationListener implements android.location.LocationListener {

        private boolean geocode;

        LocationListener(boolean geocode) {
            this.geocode = geocode;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                stopLocationUpdates();
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

    private class GMSLocationListener extends com.google.android.gms.location.LocationCallback {

        private boolean geocode;

        GMSLocationListener(boolean geocode) {
            this.geocode = geocode;
        }

        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null && locationResult.getLocations().size() > 0) {
                stopLocationUpdates();
                handleLocation(locationResult.getLocations().get(0), geocode);
            }
        }
    }

    public AndroidLocationService(Context c) {
        context = c;
        timer = new Handler(Looper.getMainLooper());

        networkListener = null;
        gpsListener = null;
        gmsListener = null;

        locationCallback = null;
        lastKnownLocation = null;
    }

    @Override
    public void requestLocation(Context context, boolean geocode, @NonNull LocationCallback callback){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gmsLocationClient = gmsEnabled(context)
                ? LocationServices.getFusedLocationProviderClient(context)
                : null;

        if (locationManager == null
                || !locationEnabled(context, locationManager)
                || !hasPermissions(context)) {
            callback.onCompleted(null);
            return;
        }

        networkListener = new LocationListener(geocode);
        gpsListener = new LocationListener(geocode);
        gmsListener = new GMSLocationListener(geocode);

        locationCallback = callback;
        lastKnownLocation = getLastKnownLocation();
        if (lastKnownLocation == null && gmsLocationClient != null) {
            gmsLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> lastKnownLocation = location);
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, networkListener, Looper.getMainLooper());
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, gpsListener, Looper.getMainLooper());
        }
        if (gmsLocationClient != null) {
            gmsLocationClient.requestLocationUpdates(
                    LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                            .setNumUpdates(1),
                    gmsListener,
                    Looper.getMainLooper()
            );
        }

        timer.postDelayed(() -> {
            stopLocationUpdates();
            handleLocation(lastKnownLocation, geocode);
        }, TIMEOUT_MILLIS);
    }

    @Nullable
    private Location getLastKnownLocation() {
        if (locationManager == null) {
            return null;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            return location;
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            return location;
        }
        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location != null) {
            return location;
        }

        return null;
    }

    @Override
    public void cancel() {
        stopLocationUpdates();
        locationCallback = null;
        timer.removeCallbacksAndMessages(null);
    }

    private void stopLocationUpdates() {
        if (locationManager != null) {
            if (networkListener != null) {
                locationManager.removeUpdates(networkListener);
                networkListener = null;
            }
            if (gpsListener != null) {
                locationManager.removeUpdates(gpsListener);
                gpsListener = null;
            }
        }
        if (gmsLocationClient != null && gmsListener != null) {
            gmsLocationClient.removeLocationUpdates(gmsListener);
            gmsListener = null;
        }
    }

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    private void handleLocation(@Nullable Location location, boolean geocode) {
        if (location == null) {
            handleResultIfNecessary(null);
            return;
        }

        Observable.create((ObservableOnSubscribe<Result>) emitter ->
                emitter.onNext(buildResult(location, geocode))
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::handleResultIfNecessary)
                .subscribe();
    }

    private void handleResultIfNecessary(@Nullable Result result) {
        if (locationCallback != null) {
            locationCallback.onCompleted(result);
            locationCallback = null;
        }
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

        if (!geocoderEnabled() || !geocode) {
            return result;
        }

        List<Address> addressList = null;
        try {
            addressList = new Geocoder(context, LanguageUtils.getCurrentLocale(context))
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

    public static boolean geocoderEnabled() {
        return Geocoder.isPresent();
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
