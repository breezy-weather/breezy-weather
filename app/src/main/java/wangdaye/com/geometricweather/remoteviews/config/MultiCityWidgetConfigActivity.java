package wangdaye.com.geometricweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.db.repositories.LocationEntityRepository;
import wangdaye.com.geometricweather.db.repositories.WeatherEntityRepository;
import wangdaye.com.geometricweather.remoteviews.presenters.MultiCityWidgetIMP;

/**
 * Multi city widget config activity.
 * */

@AndroidEntryPoint
public class MultiCityWidgetConfigActivity extends AbstractWidgetConfigActivity {

    private List<Location> locationList;

    @Override
    public void initData() {
        super.initData();

        locationList = LocationEntityRepository.INSTANCE.readLocationList();
        for (int i = 0; i < locationList.size(); i ++) {
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
