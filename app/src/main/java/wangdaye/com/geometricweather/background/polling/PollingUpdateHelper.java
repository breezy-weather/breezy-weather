package wangdaye.com.geometricweather.background.polling;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Polling updateRotation helper.
 * */

public class PollingUpdateHelper {

    private Context context;

    private LocationHelper locationHelper;
    private WeatherHelper weatherHelper;

    private List<Location> locationList;

    private OnPollingUpdateListener listener;

    public PollingUpdateHelper(Context context, List<Location> locationList) {
        this.context = context;
        this.locationHelper = new LocationHelper(context);
        this.weatherHelper = new WeatherHelper();
        this.locationList = locationList;
    }

    // control.

    public void pollingUpdate() {
        requestData(0, false);
    }

    public void cancel() {
        locationHelper.cancel();
        weatherHelper.cancel();
    }

    private void requestData(int position, boolean located) {
        Weather old = DatabaseHelper.getInstance(context).readWeather(locationList.get(position));
        if (old != null && old.isValid(0.25F)) {
            new RequestWeatherCallback(old, position, locationList.size()).requestWeatherSuccess(
                    old,
                    DatabaseHelper.getInstance(context).readHistory(old),
                    locationList.get(position)
            );
            return;
        }
        if (locationList.get(position).isCurrentPosition() && !located) {
            locationHelper.requestLocation(
                    context, locationList.get(position), true,
                    new RequestLocationCallback(position, locationList.size())
            );
        } else {
            weatherHelper.requestWeather(
                    context, locationList.get(position),
                    new RequestWeatherCallback(old, position, locationList.size())
            );
        }
    }

    // interface.

    public interface OnPollingUpdateListener {
        void onUpdateCompleted(Location location, Weather weather, Weather old,
                               boolean succeed, int index, int total);
        void onPollingCompleted();
    }

    public void setOnPollingUpdateListener(OnPollingUpdateListener l) {
        this.listener = l;
    }

    // on request location listener.

    private class RequestLocationCallback implements LocationHelper.OnRequestLocationListener {

        private int index;
        private int total;

        RequestLocationCallback(int index, int total) {
            this.index = index;
            this.total = total;
        }

        @Override
        public void requestLocationSuccess(Location requestLocation) {
            locationList.set(index, requestLocation);

            if (requestLocation.isUsable()) {
                requestData(index, true);
            } else {
                requestLocationFailed(requestLocation);
                Toast.makeText(
                        context,
                        context.getString(R.string.feedback_not_yet_location),
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void requestLocationFailed(Location requestLocation) {
            if (locationList.get(index).isUsable()) {
                requestData(index, true);
            } else {
                new RequestWeatherCallback(null, index, total)
                        .requestWeatherFailed(locationList.get(index));
            }
        }
    }

    // on request weather listener.

    private class RequestWeatherCallback implements WeatherHelper.OnRequestWeatherListener {

        @Nullable private Weather old;
        private int index;
        private int total;

        RequestWeatherCallback(@Nullable Weather old, int index, int total) {
            this.old = old;
            this.index = index;
            this.total = total;
        }

        @Override
        public void requestWeatherSuccess(@Nullable Weather weather, @Nullable History history,
                                          @NonNull Location requestLocation) {
            if (weather != null
                    && (old == null
                    || weather.base.timeStamp != old.base.timeStamp)) {
                if (listener != null) {
                    listener.onUpdateCompleted(
                            locationList.get(index), weather, old, true, index, total);
                }
                IntentHelper.sendBackgroundUpdateBroadcast(context, locationList.get(index));

                if (index + 1 < locationList.size()) {
                    requestData(index + 1, false);
                } else if (listener != null) {
                    listener.onPollingCompleted();
                }
            } else {
                requestWeatherFailed(requestLocation);
            }
        }

        @Override
        public void requestWeatherFailed(@NonNull Location requestLocation) {
            if (listener != null) {
                listener.onUpdateCompleted(requestLocation, old, old, false, index, total);
            }
            if (index + 1 < locationList.size()) {
                requestData(index + 1, false);
            } else if (listener != null) {
                listener.onPollingCompleted();
            }
        }
    }
}
