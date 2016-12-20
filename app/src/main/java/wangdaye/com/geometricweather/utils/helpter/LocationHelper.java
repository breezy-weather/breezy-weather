package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.BaiduLocation;

/**
 * Location utils.
 * */

public class LocationHelper {
    // data
    private LocationClient client;

    /** <br> life cycle. */

    public LocationHelper(Context c) {
        client = new LocationClient(c);
    }

    /** <br> data. */

    public void requestLocation(Context c, Location location, OnRequestLocationListener l) {
        NetworkInfo info = ((ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            BaiduLocation.requestLocation(client, new SimpleLocationListener(c, location, l));
        } else {
            l.requestLocationFailed(location);
        }
    }

    public void cancel() {
        if (client != null) {
            client.stop();
        }
    }

    /** <br> interface */

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation);
        void requestLocationFailed(Location requestLocation);
    }

    private static class SimpleLocationListener implements BDLocationListener {
        // data
        private Context context;
        private Location location;
        private OnRequestLocationListener listener;

        SimpleLocationListener(Context c, Location location, OnRequestLocationListener l) {
            this.context = c;
            this.location = location;
            this.listener = l;
        }

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeGpsLocation: // GPS定位结果
                case BDLocation.TypeNetWorkLocation: // 网络定位结果
                case BDLocation.TypeOffLineLocation: // 离线定位
                    if (listener != null) {
                        location.local = true;
                        location.cnty = bdLocation.getCountry();
                        location.lat = String.valueOf(bdLocation.getLatitude());
                        location.lon = String.valueOf(bdLocation.getLongitude());
                        location.prov = bdLocation.getProvince().split("省")[0];

                        String[] searchResults;
                        if (bdLocation.getCountryCode().equals("0")) {
                            searchResults = DatabaseHelper.getInstance(context).searchCityId(
                                    bdLocation.getDistrict(), bdLocation.getCity(), bdLocation.getProvince());
                        } else {
                            searchResults = DatabaseHelper.getInstance(context).searchOverseaCityId(bdLocation.getCity());
                        }
                        if (searchResults[0].equals(Location.NULL_ID)) {
                            listener.requestLocationFailed(location);
                        } else {
                            location.cityId = searchResults[0];
                            location.city = searchResults[1];
                            listener.requestLocationSuccess(location);
                        }
                    }
                    break;

                default:
                    if (listener != null) {
                        listener.requestLocationFailed(location);
                    }
                    break;
            }
        }
    }
}
