package wangdaye.com.geometricweather.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class SearchActivityRepository {

    private final WeatherHelper mWeatherHelper;
    private final SharedPreferences mSharedPreferences;

    private static final String PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG";
    private static final String KEY_DISABLED_SOURCES = "DISABLED_SOURCES";

    @Inject
    SearchActivityRepository(@ApplicationContext Context context, WeatherHelper weatherHelper) {
        mWeatherHelper = weatherHelper;
        mSharedPreferences = context.getSharedPreferences(
                PREFERENCE_SEARCH_CONFIG, Context.MODE_PRIVATE);
    }
    
    public void searchLocationList(Context context, String query, List<WeatherSource> enabledSources,
                                   AsyncHelper.Callback<List<Location>> callback) {
        mWeatherHelper.requestLocation(context, query, enabledSources, new WeatherHelper.OnRequestLocationListener() {
            @Override
            public void requestLocationSuccess(String query, List<Location> locationList) {
                callback.call(locationList, true);
            }

            @Override
            public void requestLocationFailed(String query) {
                callback.call(null, true);
            }
        });
    }

    public List<WeatherSource> getValidWeatherSources() {
        WeatherSource[] totals = WeatherSource.ACCU.getDeclaringClass().getEnumConstants();

        String value = mSharedPreferences.getString(KEY_DISABLED_SOURCES, "");
        if (TextUtils.isEmpty(value)) {
            return Arrays.asList(totals);
        }

        String[] ids = value.split(",");
        WeatherSource[] invalids = new WeatherSource[ids.length];
        for (int i = 0; i < ids.length; i ++) {
            invalids[i] = WeatherSource.getInstance(ids[i]);
        }

        List<WeatherSource> validList = new ArrayList<>();
        List<WeatherSource> invalidList = Arrays.asList(invalids);
        for (WeatherSource source : totals) {
            if (!invalidList.contains(source)) {
                validList.add(source);
            }
        }
        return validList;
    }

    public void setValidWeatherSources(List<WeatherSource> validList) {
        WeatherSource[] totals = WeatherSource.ACCU.getDeclaringClass().getEnumConstants();

        StringBuilder b = new StringBuilder();
        for (WeatherSource source : totals) {
            if (!validList.contains(source)) {
                b.append(",").append(source.getSourceId());
            }
        }

        String value = b.length() > 0 ? b.substring(1) : "";
        mSharedPreferences.edit().putString(KEY_DISABLED_SOURCES, value).apply();
    }

    public void cancel() {
        mWeatherHelper.cancel();
    }
}
