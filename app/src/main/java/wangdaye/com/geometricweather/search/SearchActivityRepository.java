package wangdaye.com.geometricweather.search;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.settings.ConfigStore;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class SearchActivityRepository {

    private final WeatherHelper mWeatherHelper;
    private final ConfigStore mConfig;

    private @Nullable List<WeatherSource> mValidSourceCache;
    private @Nullable WeatherSource mLastDefaultSourceCache;

    private static final String PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG";
    private static final String KEY_DISABLED_SOURCES = "DISABLED_SOURCES";
    private static final String KEY_LAST_DEFAULT_SOURCE = "LAST_DEFAULT_SOURCE";

    private static final String DEFAULT_DISABLED_SOURCES_VALUE = "ENABLE_DEFAULT_SOURCE_ONLY";

    @Inject
    SearchActivityRepository(@ApplicationContext Context context, WeatherHelper weatherHelper) {
        mWeatherHelper = weatherHelper;
        mConfig = ConfigStore.getInstance(context, PREFERENCE_SEARCH_CONFIG);

        mValidSourceCache = null;
        mLastDefaultSourceCache = null;
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

    public List<WeatherSource> getValidWeatherSources(Context context) {
        WeatherSource defaultSource = SettingsManager.getInstance(context).getWeatherSource();

        if (mValidSourceCache != null && defaultSource == mLastDefaultSourceCache) {
            return mValidSourceCache;
        }

        WeatherSource[] totals = WeatherSource.ACCU.getDeclaringClass().getEnumConstants();
        if (totals == null) {
            return new ArrayList<>();
        }

        String lastDefaultSource = mConfig.getString(KEY_LAST_DEFAULT_SOURCE, "");
        mLastDefaultSourceCache = WeatherSource.getInstance(lastDefaultSource);

        String value;
        if (!defaultSource.getSourceId().equals(lastDefaultSource)) {
            // last default source is not equal to current default source which is set by user.

            // we need reset the value.
            value = DEFAULT_DISABLED_SOURCES_VALUE;
            mConfig.edit()
                    .putString(KEY_DISABLED_SOURCES, value)
                    .putString(KEY_LAST_DEFAULT_SOURCE, defaultSource.getSourceId())
                    .apply();
        } else {
            value = mConfig.getString(KEY_DISABLED_SOURCES, "");
            mConfig.edit()
                    .putString(KEY_LAST_DEFAULT_SOURCE, defaultSource.getSourceId())
                    .apply();
        }

        if (TextUtils.isEmpty(value)) {
            return Arrays.asList(totals);
        }

        if (value.equals(DEFAULT_DISABLED_SOURCES_VALUE)) {
            return Collections.singletonList(defaultSource);
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

        mValidSourceCache = validList;
        return validList;
    }

    public void setValidWeatherSources(List<WeatherSource> validList) {
        mValidSourceCache = validList;

        WeatherSource[] totals = WeatherSource.ACCU.getDeclaringClass().getEnumConstants();
        if (totals == null) {
            return;
        }

        StringBuilder b = new StringBuilder();
        for (WeatherSource source : totals) {
            if (!validList.contains(source)) {
                b.append(",").append(source.getSourceId());
            }
        }

        String value = b.length() > 0 ? b.substring(1) : "";
        mConfig.edit().putString(KEY_DISABLED_SOURCES, value).apply();
    }

    public void cancel() {
        mWeatherHelper.cancel();
    }
}
