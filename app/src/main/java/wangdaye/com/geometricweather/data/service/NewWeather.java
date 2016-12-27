package wangdaye.com.geometricweather.data.service;

import android.content.Context;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.data.api.NewWeatherApi;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Alert;
import wangdaye.com.geometricweather.data.entity.model.weather.Daily;
import wangdaye.com.geometricweather.data.entity.model.weather.Hourly;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.result.NewAlertResult;
import wangdaye.com.geometricweather.data.entity.result.NewAqiResult;
import wangdaye.com.geometricweather.data.entity.result.NewDailyResult;
import wangdaye.com.geometricweather.data.entity.result.NewHourlyResult;
import wangdaye.com.geometricweather.data.entity.result.NewLocationResult;
import wangdaye.com.geometricweather.data.entity.result.NewRealtimeResult;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Accu weather.
 * */

public class NewWeather {
    // widget
    private Call locationCall;
    private Call[] calls = new Call[5];

    // data
    private Weather weather;
    private Location requestLocation;
    private int successTime;
    private String languageCode = "en";

    /** <br> data. */

    private boolean isSucceed(Location location) {
        return successTime == 5 && location.equals(requestLocation);
    }

    private boolean isFailed(Location location) {
        return successTime < 0 || !location.equals(requestLocation);
    }

    private void loadSucceed(Location location, WeatherHelper.OnRequestWeatherListener listener) {
        successTime ++;
        if (isSucceed(location)) {
            listener.requestWeatherSuccess(weather, requestLocation);
        }
    }

    private void loadFailed(Location location, WeatherHelper.OnRequestWeatherListener listener) {
        if (!isFailed(location)) {
            successTime = -1;
            listener.requestWeatherFailed(requestLocation);
        }
    }

    public NewWeather requestNewWeather(Context c, Location location, WeatherHelper.OnRequestWeatherListener l) {
        this.weather = new Weather();
        this.requestLocation = location;
        this.successTime = 0;
        this.languageCode = LanguageUtils.getLanguageCode(c);

        requestNewRealtime(c, location, l);
        requestNewAqi(c, location, l);
        requestNewDaily(c, location, l);
        requestNewHourly(c, location, l);
        requestNewAlert(c, location, l);

        return this;
    }

    public NewWeather requestNewLocation(Context c, final String query,
                                         final LocationHelper.OnRequestWeatherLocationListener l) {
        this.languageCode = LanguageUtils.getLanguageCode(c);
        Call<List<NewLocationResult>> getAccuLocation = buildApi().getWeatherLocation(
                "Always",
                BuildConfig.NEW_WEATHER_KEY,
                query,
                languageCode);
        getAccuLocation.enqueue(new Callback<List<NewLocationResult>>() {
            @Override
            public void onResponse(Call<List<NewLocationResult>> call, Response<List<NewLocationResult>> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestWeatherLocationSuccess(query, Location.buildLocationList(response.body()));
                    } else {
                        l.requestWeatherLocationFailed(query);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NewLocationResult>> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherLocationFailed(query);
                }
            }
        });
        locationCall = getAccuLocation;
        return this;
    }

    public NewWeather requestNewLocationByGeoPosition(Context c, final String lat, final String lon,
                                                       final LocationHelper.OnRequestWeatherLocationListener l) {
        this.languageCode = LanguageUtils.getLanguageCode(c);
        Call<NewLocationResult> getAccuLocationByGeoPosition = buildApi().getWeatherLocationByGeoPosition(
                "Always",
                BuildConfig.NEW_WEATHER_KEY,
                lat + "," + lon,
                languageCode);
        getAccuLocationByGeoPosition.enqueue(new Callback<NewLocationResult>() {
            @Override
            public void onResponse(Call<NewLocationResult> call, Response<NewLocationResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestWeatherLocationSuccess(lat + "," + lon, Location.buildLocationList(response.body()));
                    } else {
                        l.requestWeatherLocationFailed(lat + "," + lon);
                    }
                }
            }

            @Override
            public void onFailure(Call<NewLocationResult> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherLocationFailed(lat + "," + lon);
                }
            }
        });
        locationCall = getAccuLocationByGeoPosition;
        return this;
    }

    private void requestNewRealtime(final Context c, final Location location,
                                    final WeatherHelper.OnRequestWeatherListener l) {
        Call<List<NewRealtimeResult>> getAccuRealtime = buildApi().getNewRealtime(
                location.cityId,
                BuildConfig.NEW_WEATHER_KEY,
                languageCode,
                true);
        getAccuRealtime.enqueue(new Callback<List<NewRealtimeResult>>() {
            @Override
            public void onResponse(Call<List<NewRealtimeResult>> call, Response<List<NewRealtimeResult>> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            weather.base.buildBase(location, response.body().get(0));
                            weather.realTime.buildRealTime(c, response.body().get(0));
                            weather.index.buildIndex(c, response.body().get(0));
                            loadSucceed(location, l);
                        }
                    } else {
                        loadFailed(location, l);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NewRealtimeResult>> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[0] = getAccuRealtime;
    }

    private void requestNewDaily(final Context c, final Location location,
                                 final WeatherHelper.OnRequestWeatherListener l) {
        Call<NewDailyResult> getAccuDaily = buildApi().getNewDaily(
                location.cityId,
                BuildConfig.NEW_WEATHER_KEY,
                languageCode,
                true,
                true);
        getAccuDaily.enqueue(new Callback<NewDailyResult>() {
            @Override
            public void onResponse(Call<NewDailyResult> call, Response<NewDailyResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            weather.realTime.buildRealTime(response.body());
                            for (int i = 0; i < response.body().DailyForecasts.size(); i ++) {
                                weather.dailyList.add(new Daily().buildDaily(c, response.body().DailyForecasts.get(i)));
                            }
                            weather.index.buildIndex(c, response.body());
                            loadSucceed(location, l);
                        }
                    } else {
                        loadFailed(location, l);
                    }
                }
            }

            @Override
            public void onFailure(Call<NewDailyResult> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[1] = getAccuDaily;
    }

    private void requestNewHourly(final Context c, final Location location,
                                  final WeatherHelper.OnRequestWeatherListener l) {
        Call<List<NewHourlyResult>> getAccuHourly = buildApi().getNewHourly(
                location.cityId,
                BuildConfig.NEW_WEATHER_KEY,
                languageCode,
                true);
        getAccuHourly.enqueue(new Callback<List<NewHourlyResult>>() {
            @Override
            public void onResponse(Call<List<NewHourlyResult>> call, Response<List<NewHourlyResult>> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            for (int i = 0; i < response.body().size(); i ++) {
                                weather.hourlyList.add(new Hourly().buildHourly(c, response.body().get(i)));
                            }
                            loadSucceed(location, l);
                        }
                    } else {
                        loadFailed(location, l);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NewHourlyResult>> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[2] = getAccuHourly;
    }

    private void requestNewAlert(final Context c, final Location location,
                                 final WeatherHelper.OnRequestWeatherListener l) {
        Call<List<NewAlertResult>> getAccuAlert = buildApi().getNewAlert(
                location.cityId,
                BuildConfig.NEW_WEATHER_KEY,
                languageCode,
                true);
        getAccuAlert.enqueue(new Callback<List<NewAlertResult>>() {
            @Override
            public void onResponse(Call<List<NewAlertResult>> call, Response<List<NewAlertResult>> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            for (int i = 0; i < response.body().size(); i ++) {
                                weather.alertList.add(new Alert().buildAlert(c, response.body().get(i)));
                            }
                            loadSucceed(location, l);
                        }
                    } else {
                        loadFailed(location, l);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NewAlertResult>> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[3] = getAccuAlert;
    }

    private void requestNewAqi(final Context c, final Location location,
                               final WeatherHelper.OnRequestWeatherListener l) {
        Call<NewAqiResult> getAccuAqi = buildApi().getNewAqi(
                location.cityId,
                BuildConfig.NEW_AQI_KEY);
        getAccuAqi.enqueue(new Callback<NewAqiResult>() {
            @Override
            public void onResponse(Call<NewAqiResult> call, Response<NewAqiResult> response) {
                if (l != null) {
                    if (response.isSuccessful()) {
                        if (!isFailed(location)) {
                            weather.aqi.buildAqi(c, response.body());
                            weather.index.buildIndex(c, response.body());
                            loadSucceed(location, l);
                        }
                    } else {
                        loadFailed(location, l);
                    }
                }
            }

            @Override
            public void onFailure(Call<NewAqiResult> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[4] = getAccuAqi;
    }

    public void cancel() {
        if (locationCall != null) {
            locationCall.cancel();
        }
        for (Call call : calls) {
            if (call != null) {
                call.cancel();
            }
        }
    }

    public static NewWeather getService() {
        return new NewWeather();
    }

    private NewWeatherApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.NEW_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((NewWeatherApi.class));
    }
}
