package wangdaye.com.geometricweather.location.services;

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

import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;

public class GMSLocationService extends LocationService {

    private final Context mContext;
    private final Handler mTimer;

    private final FusedLocationProviderClient mGMSClient;
    @Nullable private GMSLocationListener mGMSLocationListener;
    @Nullable private LocationCallback mLocationCallback;
    @Nullable private Location mLastKnownLocation;

    private static final long TIMEOUT_MILLIS = 10 * 1000;

    private class GMSLocationListener extends com.google.android.gms.location.LocationCallback {

        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null && locationResult.getLocations().size() > 0) {
                handleLocation(locationResult.getLocations().get(0));
            }
        }
    }

    public GMSLocationService(Context c) {
        mContext = c;
        mTimer = new Handler(Looper.getMainLooper());

        mGMSClient = LocationServices.getFusedLocationProviderClient(mContext);
        mGMSLocationListener = null;
        mLocationCallback = null;
        mLastKnownLocation = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback) {
        if (!hasPermissions(context)) {
            callback.onCompleted(null);
            return;
        }

        mGMSLocationListener = new GMSLocationListener();
        mLocationCallback = callback;
        mLastKnownLocation = null;

        mGMSClient.getLastLocation().addOnSuccessListener(location -> mLastKnownLocation = location);

        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mGMSClient.requestLocationUpdates(request, mGMSLocationListener, Looper.getMainLooper());

        mTimer.postDelayed(() -> {
            if (mGMSLocationListener != null) {
                mGMSClient.removeLocationUpdates(mGMSLocationListener);
                mGMSLocationListener = null;
            }
            handleLocation(mLastKnownLocation);
        }, TIMEOUT_MILLIS);
    }

    @Override
    public void cancel() {
        mTimer.removeCallbacksAndMessages(null);

        if (mGMSLocationListener != null) {
            mGMSClient.removeLocationUpdates(mGMSLocationListener);
            mGMSLocationListener = null;
        }

        mLocationCallback = null;
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

        AsyncHelper.runOnIO(emitter -> emitter.send(buildResult(location)), this::handleResultIfNecessary);
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

    public static boolean isEnabled(Context context) {
        return GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }
}
