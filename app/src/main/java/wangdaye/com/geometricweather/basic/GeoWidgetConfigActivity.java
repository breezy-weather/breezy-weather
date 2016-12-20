package wangdaye.com.geometricweather.basic;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Geometric widget configuration activity.
 * */

public abstract class GeoWidgetConfigActivity extends GeoActivity
        implements WeatherHelper.OnRequestWeatherListener, LocationHelper.OnRequestLocationListener  {
    // data
    private Location locationNow;
    private List<Location> locationList;
    private List<String> nameList;

    private WeatherHelper weatherHelper;
    private LocationHelper locationHelper;

    /** <br> life cycle. */

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();

            if (locationNow.isLocal()) {
                locationHelper.requestLocation(this, locationNow, this);
            } else {
                weatherHelper.requestWeather(this, locationNow, this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        weatherHelper.cancel();
        locationHelper.cancel();
    }

    private void initData() {
        this.nameList = new ArrayList<>();
        locationList = DatabaseHelper.getInstance(this).readLocationList();
        for (Location l : locationList) {
            nameList.add(l.isLocal() ? getString(R.string.local) : l.city);
        }
        this.locationNow = locationList.get(0);

        this.weatherHelper = new WeatherHelper();
        this.locationHelper = new LocationHelper(this);
    }

    public abstract void initWidget();

    public abstract void refreshWidgetView(Weather weather);

    /** <br> data. */

    public void setLocationNow(Location location) {
        this.locationNow = location;
    }

    public Location getLocationNow() {
        return locationNow;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public List<String> getNameList() {
        return nameList;
    }

    /** <br> interface. */

    // on request name listener.

    @Override
    public void requestLocationSuccess(Location requestLocation) {
        if (!requestLocation.isUsable()) {
            requestLocationFailed(requestLocation);
        } else {
            DatabaseHelper.getInstance(this).writeLocation(requestLocation);
            weatherHelper.requestWeather(this, requestLocation, this);
        }
    }

    @Override
    public void requestLocationFailed(Location requestLocation) {
        SnackbarUtils.showSnackbar(getString(R.string.feedback_location_failed));
        if (requestLocation.isUsable()) {
            weatherHelper.requestWeather(this, requestLocation, this);
        }
    }

    // on request realTimeWeather listener.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        if (weather == null) {
            requestWeatherFailed(requestLocation);
        } else {
            refreshWidgetView(weather);
            DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
            DatabaseHelper.getInstance(this).writeHistory(weather);
        }
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        refreshWidgetView(weather);
        SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
    }
}
