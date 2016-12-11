package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

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

    public void requestLocation(Context c, OnRequestLocationListener l) {
        NetworkInfo info = ((ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            BaiduLocation.requestLocation(client, new SimpleBDLocationListener(l));
        } else {
            l.requestLocationFailed();
        }
    }

    public void cancel() {
        if (client != null) {
            client.stop();
        }
    }

    /** <br> interface */

    public interface OnRequestLocationListener {
        void requestLocationSuccess(String locationName);
        void requestLocationFailed();
    }

    private static class SimpleBDLocationListener implements BDLocationListener {
        // data
        private OnRequestLocationListener l;

        SimpleBDLocationListener(OnRequestLocationListener l) {
            this.l = l;
        }

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeGpsLocation: // GPS定位结果
                case BDLocation.TypeNetWorkLocation: // 网络定位结果
                case BDLocation.TypeOffLineLocation: // 离线定位
                    if (l != null) {
                        l.requestLocationSuccess(bdLocation.getCity().split("市")[0]);
                    }
                    break;
                default:
                    if (l != null) {
                        l.requestLocationFailed();
                    }
                    break;
            }
        }
    }
}
