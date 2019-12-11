package wangdaye.com.geometricweather.location.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

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

public class GMSLocationService extends LocationService {

    private Context context;
    private Handler timer;

    private FusedLocationProviderClient client;
    @Nullable private GMSLocationListener locationListener;
    @Nullable private LocationCallback locationCallback;
    @Nullable private Location lastKnownLocation;

    private static final long TIMEOUT_MILLIS = 10 * 1000;

    private class GMSLocationListener extends com.google.android.gms.location.LocationCallback {

        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null && locationResult.getLocations().size() > 0) {
                handleLocation(locationResult.getLocations().get(0));
            }
        }
    }

    public GMSLocationService(Context c) {
        context = c;
        timer = new Handler(Looper.getMainLooper());

        client = LocationServices.getFusedLocationProviderClient(context);
        locationListener = null;
        locationCallback = null;
        lastKnownLocation = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback) {
        if (!hasPermissions(context)) {
            callback.onCompleted(null);
            return;
        }

        locationListener = new GMSLocationListener();
        locationCallback = callback;
        lastKnownLocation = null;

        client.getLastLocation().addOnSuccessListener(location -> lastKnownLocation = location);

        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        client.requestLocationUpdates(request, locationListener, Looper.getMainLooper());

        timer.postDelayed(() -> {
            if (locationListener != null) {
                client.removeLocationUpdates(locationListener);
                locationListener = null;
            }
            handleLocation(lastKnownLocation);
        }, TIMEOUT_MILLIS);
    }

    @Override
    public void cancel() {
        timer.removeCallbacksAndMessages(null);

        if (locationListener != null) {
            client.removeLocationUpdates(locationListener);
            locationListener = null;
        }

        locationCallback = null;
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

        Observable.create((ObservableOnSubscribe<Result>) emitter ->
                emitter.onNext(buildResult(location))
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
    private Result buildResult(@NonNull Location location) {
        Result result = new Result((float) location.getLatitude(), (float) location.getLongitude());
        result.hasGeocodeInformation = false;

        if (!Geocoder.isPresent()) {
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

    public static boolean isEnabled(Context context) {
        return GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }
}
