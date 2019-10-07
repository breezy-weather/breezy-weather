package wangdaye.com.geometricweather.basic.model.option.provider;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionUtils;

public enum LocationProvider {

    BAIDU("baidu"),
    BAIDU_IP("baidu_ip"),
    AMAP("amap"),
    NATIVE("native");

    private String providerId;

    LocationProvider(String id) {
        providerId = id;
    }

    public String getProviderId() {
        return providerId;
    }

    @Nullable
    public String getProviderName(Context context) {
        return OptionUtils.getNameByValue(
                context,
                providerId,
                R.array.location_services,
                R.array.location_service_values
        );
    }
}
