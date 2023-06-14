package org.breezyweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.db.repositories.LocationEntityRepository;
import org.breezyweather.db.repositories.WeatherEntityRepository;
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP;
import org.breezyweather.R;

/**
 * Multi city widget config activity.
 * */

@AndroidEntryPoint
public class MultiCityWidgetConfigActivity extends AbstractWidgetConfigActivity {

    private List<Location> locationList;

    @Override
    public void initData() {
        super.initData();

        locationList = LocationEntityRepository.INSTANCE.readLocationList(this);
        for (int i = 0; i < locationList.size(); i++) {
            locationList.set(
                    i, Location.copy(
                            locationList.get(i),
                            WeatherEntityRepository.INSTANCE.readWeather(locationList.get(i))
                    )
            );
        }
    }

    @Override
    public void initView() {
        super.initView();
        mCardStyleContainer.setVisibility(View.VISIBLE);
        mCardAlphaContainer.setVisibility(View.VISIBLE);
        mTextColorContainer.setVisibility(View.VISIBLE);
        mTextSizeContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public RemoteViews getRemoteViews() {
        return MultiCityWidgetIMP.getRemoteViews(
                this,
                locationList,
                cardStyleValueNow, cardAlpha,
                textColorValueNow, textSize
        );
    }

    @Override
    public String getConfigStoreName() {
        return getString(R.string.sp_widget_multi_city);
    }
}
