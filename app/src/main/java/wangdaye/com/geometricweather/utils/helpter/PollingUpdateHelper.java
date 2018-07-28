package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;

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
        if (locationList.get(position).isLocal() && !located) {
            locationHelper.requestLocation(
                    context, locationList.get(position), new RequestLocationCallback(position));
        } else {
            Weather old = DatabaseHelper.getInstance(context).readWeather(locationList.get(position));
            if (old != null && old.isValid(0.15F)) {
                new RequestWeatherCallback(old, position)
                        .requestWeatherSuccess(old, locationList.get(position));
                return;
            }
            weatherHelper.requestWeather(
                    context, locationList.get(position), new RequestWeatherCallback(old, position));
        }
    }

    /**
     * compare weather by time.
     *
     * @return 1: weather is newer than old.
     *         0: weather equal old.
     *        -1: weather is older than old.
     * */
    private int compareWeatherData(@Nullable Weather weather, @Nullable Weather old) {
        if (weather == null) {
            return -1;
        } else if (old == null) {
            return 1;
        } else {
            try {
                String[] newDates = weather.base.date.split("-");
                String[] newTimes = weather.base.time.split(":");
                String[] oldDates = old.base.date.split("-");
                String[] oldTimes = old.base.time.split(":");
                
                int[][] dates = new int[][] {
                        new int[] {Integer.parseInt(newDates[0]), Integer.parseInt(oldDates[0])},
                        new int[] {Integer.parseInt(newDates[1]), Integer.parseInt(oldDates[1])},
                        new int[] {Integer.parseInt(newDates[2]), Integer.parseInt(oldDates[2])},
                        new int[] {Integer.parseInt(newTimes[0]), Integer.parseInt(oldTimes[0])},
                        new int[] {Integer.parseInt(newTimes[1]), Integer.parseInt(oldTimes[1])}};
                for (int[] d : dates) {
                    if (d[0] > d[1]) {
                        return 1;
                    } else if (d[0] < d[1]) {
                        return -1;
                    }
                }
                return 0;
            } catch (Exception ignore) {
                // do nothing.
            }
            return -1;
        }
    }

    // interface.

    public interface OnPollingUpdateListener {
        void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed);
        void onPollingCompleted();
    }

    public void setOnPollingUpdateListener(OnPollingUpdateListener l) {
        this.listener = l;
    }

    // on request location listener.

    private class RequestLocationCallback implements LocationHelper.OnRequestLocationListener {

        private int position;

        RequestLocationCallback(int position) {
            this.position = position;
        }

        @Override
        public void requestLocationSuccess(Location requestLocation, boolean locationChanged) {
            locationList.set(position, requestLocation);

            Weather old = DatabaseHelper.getInstance(context).readWeather(locationList.get(position));
            if (old != null && old.isValid(0.15F) && !locationChanged) {
                new RequestWeatherCallback(old, position)
                        .requestWeatherSuccess(old, locationList.get(position));
                return;
            }

            if (requestLocation.isUsable()) {
                requestData(position, true);
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
            if (locationList.get(position).isUsable()) {
                requestData(position, true);
            } else {
                new RequestWeatherCallback(null, position)
                        .requestWeatherFailed(locationList.get(position));
            }
        }
    }

    // on request weather listener.

    private class RequestWeatherCallback implements WeatherHelper.OnRequestWeatherListener {

        @Nullable
        private Weather old;
        private int position;

        RequestWeatherCallback(@Nullable Weather old, int position) {
            this.old = old;
            this.position = position;
        }

        @Override
        public void requestWeatherSuccess(Weather weather, Location requestLocation) {
            int compareResult = compareWeatherData(weather, old);
            if (compareResult >= 0) {
                if (compareResult > 0) {
                    DatabaseHelper.getInstance(context).writeWeather(locationList.get(position), weather);
                    DatabaseHelper.getInstance(context).writeHistory(weather);
                }
                if (listener != null) {
                    listener.onUpdateCompleted(locationList.get(position), weather, old, true);
                }
                if (position + 1 < locationList.size()) {
                    requestData(position + 1, false);
                } else if (listener != null) {
                    listener.onPollingCompleted();
                }
            } else {
                requestWeatherFailed(requestLocation);
            }
        }

        @Override
        public void requestWeatherFailed(Location requestLocation) {
            if (listener != null) {
                listener.onUpdateCompleted(requestLocation, old, old, false);
            }
            if (position + 1 < locationList.size()) {
                requestData(position + 1, false);
            } else if (listener != null) {
                listener.onPollingCompleted();
            }
        }
    }
}
