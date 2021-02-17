package wangdaye.com.geometricweather.search;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.utils.helpters.AsyncHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class SearchActivityRepository {

    private final WeatherHelper mWeatherHelper;
    private final SharedPreferences mSharedPreferences;

    private static final String PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG";
    private static final String KEY_MULTI_SOURCE_ENABLED = "MULTI_SOURCE_ENABLED";

    @Inject
    SearchActivityRepository(@ApplicationContext Context context, WeatherHelper weatherHelper) {
        mWeatherHelper = weatherHelper;
        mSharedPreferences = context.getSharedPreferences(
                PREFERENCE_SEARCH_CONFIG, Context.MODE_PRIVATE);
    }
    
    public void searchLocationList(Context context, String query, boolean multiSource,
                                   AsyncHelper.Callback<List<Location>> callback) {
        mWeatherHelper.requestLocation(context, query, multiSource, new WeatherHelper.OnRequestLocationListener() {
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

    public boolean isMultiSourceEnabled() {
        return mSharedPreferences.getBoolean(KEY_MULTI_SOURCE_ENABLED, false);
    }

    public void setMultiSourceEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(KEY_MULTI_SOURCE_ENABLED, enabled).apply();
    }

    public void cancel() {
        mWeatherHelper.cancel();
    }
}
