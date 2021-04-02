package wangdaye.com.geometricweather.search.ui.adapter;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;

class WeatherSourceModel {

    private final WeatherSource mSource;
    private boolean mEnabled;

    WeatherSourceModel(WeatherSource mSource, boolean mEnabled) {
        this.mSource = mSource;
        this.mEnabled = mEnabled;
    }

    WeatherSource getSource() {
        return mSource;
    }

    boolean isEnabled() {
        return mEnabled;
    }

    void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }
}
