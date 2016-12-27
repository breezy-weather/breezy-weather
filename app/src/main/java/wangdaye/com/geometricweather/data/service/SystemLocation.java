package wangdaye.com.geometricweather.data.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;

/**
 * System location.
 * */

public class SystemLocation {

    public static Location requestLocation(Context context, Location location) {
        if (Geocoder.isPresent()) {
            return null;
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        for (String provider : providerList) {
            android.location.Location result = locationManager.getLastKnownLocation(provider);
            if (result != null) {
                try {
                    List<Address> addressList = new Geocoder(context)
                            .getFromLocation(result.getLatitude(), result.getLongitude(), 1);
                    if (addressList.size() > 0) {
                        Address address = addressList.get(0);
                        location.local = true;
                        location.city = address.getSubLocality();
                        location.prov = address.getAdminArea();
                        location.cnty = address.getCountryName();
                        location.lat = String.valueOf(address.getLatitude());
                        location.lon = String.valueOf(address.getLongitude());
                        return location;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static Location requestLatAndLon(Context context, Location location) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        for (String provider : providerList) {
            android.location.Location result = locationManager.getLastKnownLocation(provider);
            if (result != null) {
                location.local = true;
                location.lat = String.valueOf(result.getLatitude());
                location.lon = String.valueOf(result.getLongitude());
                return location;
            }
        }

        return null;
    }
}
