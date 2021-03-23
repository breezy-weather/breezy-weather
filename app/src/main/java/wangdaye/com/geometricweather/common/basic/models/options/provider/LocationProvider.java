package wangdaye.com.geometricweather.common.basic.models.options.provider;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options._utils.Utils;

public enum LocationProvider {

    BAIDU("baidu"),
    BAIDU_IP("baidu_ip"),
    AMAP("amap"),
    NATIVE("native");

    private final String providerId;

    LocationProvider(String id) {
        providerId = id;
    }

    public String getProviderId() {
        return providerId;
    }

    @Nullable
    public String getProviderName(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                providerId,
                R.array.location_services,
                R.array.location_service_values
        );
    }

    public static LocationProvider getInstance(String value) {
        switch (value) {
            case "baidu_ip":
                return BAIDU_IP;

            case "baidu":
                return BAIDU;

            case "amap":
                return AMAP;

            default:
                return NATIVE;
        }
    }
}
