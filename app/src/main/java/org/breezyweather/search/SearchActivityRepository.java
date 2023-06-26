package org.breezyweather.search;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlin.Pair;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.provider.WeatherSource;
import org.breezyweather.common.utils.helpers.AsyncHelper;
import org.breezyweather.main.utils.RequestErrorType;
import org.breezyweather.settings.ConfigStore;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.weather.WeatherHelper;

public class SearchActivityRepository {

    private final WeatherHelper mWeatherHelper;
    private final ConfigStore mConfig;

    private @Nullable WeatherSource mLastDefaultSourceCache;

    private static final String PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG";
    private static final String KEY_DISABLED_SOURCES = "DISABLED_SOURCES";
    private static final String KEY_LAST_DEFAULT_SOURCE = "LAST_DEFAULT_SOURCE";

    private static final String DEFAULT_DISABLED_SOURCES_VALUE = "ENABLE_DEFAULT_SOURCE_ONLY";

    @Inject
    SearchActivityRepository(@ApplicationContext Context context, WeatherHelper weatherHelper) {
        mWeatherHelper = weatherHelper;
        mConfig = new ConfigStore(context, PREFERENCE_SEARCH_CONFIG);

        mLastDefaultSourceCache = null;
    }
    
    public void searchLocationList(Context context, String query, WeatherSource enabledSource,
                                   AsyncHelper.Callback<Pair<List<Location>, RequestErrorType>> callback) {
        mWeatherHelper.requestSearchLocations(context, query, enabledSource, new WeatherHelper.OnRequestLocationListener() {
            @Override
            public void requestLocationSuccess(String query, List<Location> locationList) {
                callback.call(new Pair<>(locationList, null), true);
            }

            @Override
            public void requestLocationFailed(String query, RequestErrorType requestErrorType) {
                callback.call(new Pair<>(null, requestErrorType), true);
            }
        });
    }

    public WeatherSource getValidWeatherSource(Context context) {
        WeatherSource defaultSource = SettingsManager.getInstance(context).getWeatherSource();

        if (defaultSource == mLastDefaultSourceCache) {
            return mLastDefaultSourceCache;
        }

        WeatherSource[] totals = WeatherSource.class.getEnumConstants();
        if (totals == null) {
            return null;
        }

        String lastDefaultSource = mConfig.getString(KEY_LAST_DEFAULT_SOURCE, "");
        mLastDefaultSourceCache = WeatherSource.getInstance(lastDefaultSource);

        String value;
        if (!defaultSource.getId().equals(lastDefaultSource)) {
            // last default source is not equal to current default source which is set by user.

            // we need reset the value.
            mConfig.edit()
                    .putString(KEY_DISABLED_SOURCES, DEFAULT_DISABLED_SOURCES_VALUE)
                    .putString(KEY_LAST_DEFAULT_SOURCE, defaultSource.getId())
                    .apply();
        } else {
            mConfig.edit()
                    .putString(KEY_LAST_DEFAULT_SOURCE, defaultSource.getId())
                    .apply();
        }

        return defaultSource;
    }

    public void setValidWeatherSource(WeatherSource weatherSource) {
        WeatherSource[] totals = WeatherSource.class.getEnumConstants();
        if (totals == null) {
            return;
        }

        StringBuilder b = new StringBuilder();
        for (WeatherSource source : totals) {
            if (!weatherSource.equals(source)) {
                b.append(",").append(source.getId());
            }
        }

        String value = b.length() > 0 ? b.substring(1) : "";
        mConfig.edit().putString(KEY_DISABLED_SOURCES, value).apply();
    }

    public void cancel() {
        mWeatherHelper.cancel();
    }
}
