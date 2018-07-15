package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.data.api.AccuWeatherApi;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Alert;
import wangdaye.com.geometricweather.data.entity.model.weather.Daily;
import wangdaye.com.geometricweather.data.entity.model.weather.Hourly;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuAlertResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuAqiResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuDailyResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuLocationResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuRealtimeResult;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Accu weather service.
 * */

public class AccuWeatherService {

    private Call locationCall;
    private Call[] calls = new Call[5];

    private Weather weather;
    private Location requestLocation;
    private int successTime;
    private String languageCode = "en";

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

    AccuWeatherService requestWeather(Context c, Location location, WeatherHelper.OnRequestWeatherListener l) {
        this.weather = new Weather();
        this.requestLocation = location;
        this.successTime = 0;
        this.languageCode = LanguageUtils.getLanguageCode(c);

        requestRealtime(c, location, l);
        requestAqi(c, location, l);
        requestDaily(c, location, l);
        requestHourly(c, location, l);
        requestAlert(c, location, l);

        return this;
    }

    AccuWeatherService requestLocation(Context c, final String query,
                                       final LocationHelper.OnRequestWeatherLocationListener l) {
        this.languageCode = LanguageUtils.getLanguageCode(c);
        Call<List<AccuLocationResult>> getAccuLocation = buildApi().getWeatherLocation(
                "Always",
                BuildConfig.ACCU_WEATHER_KEY,
                query,
                languageCode);
        getAccuLocation.enqueue(new Callback<List<AccuLocationResult>>() {
            @Override
            public void onResponse(Call<List<AccuLocationResult>> call, Response<List<AccuLocationResult>> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestWeatherLocationSuccess(query, Location.buildLocationListByAccuResult(response.body()));
                    } else {
                        l.requestWeatherLocationFailed(query);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AccuLocationResult>> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherLocationFailed(query);
                }
            }
        });
        locationCall = getAccuLocation;
        return this;
    }

    AccuWeatherService requestLocationByGeoPosition(Context c, final String lat, final String lon,
                                                    final LocationHelper.OnRequestWeatherLocationListener l) {
        this.languageCode = LanguageUtils.getLanguageCode(c);
        Call<AccuLocationResult> getAccuLocationByGeoPosition = buildApi().getWeatherLocationByGeoPosition(
                "Always",
                BuildConfig.ACCU_WEATHER_KEY,
                lat + "," + lon,
                languageCode);
        getAccuLocationByGeoPosition.enqueue(new Callback<AccuLocationResult>() {
            @Override
            public void onResponse(Call<AccuLocationResult> call, Response<AccuLocationResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestWeatherLocationSuccess(lat + "," + lon, Location.buildLocationList(response.body()));
                    } else {
                        l.requestWeatherLocationFailed(lat + "," + lon);
                    }
                }
            }

            @Override
            public void onFailure(Call<AccuLocationResult> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherLocationFailed(lat + "," + lon);
                }
            }
        });
        locationCall = getAccuLocationByGeoPosition;
        return this;
    }

    void requestRealtime(final Context c, final Location location,
                         final WeatherHelper.OnRequestWeatherListener l) {
        Call<List<AccuRealtimeResult>> getAccuRealtime = buildApi().getRealtime(
                location.cityId,
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true);
        getAccuRealtime.enqueue(new Callback<List<AccuRealtimeResult>>() {
            @Override
            public void onResponse(Call<List<AccuRealtimeResult>> call, Response<List<AccuRealtimeResult>> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            weather.base.buildBase(c, location, response.body().get(0));
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
            public void onFailure(Call<List<AccuRealtimeResult>> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[0] = getAccuRealtime;
    }

    private void requestDaily(final Context c, final Location location,
                              final WeatherHelper.OnRequestWeatherListener l) {
        Call<AccuDailyResult> getAccuDaily = buildApi().getDaily(
                location.cityId,
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true,
                true);
        getAccuDaily.enqueue(new Callback<AccuDailyResult>() {
            @Override
            public void onResponse(Call<AccuDailyResult> call, Response<AccuDailyResult> response) {
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
            public void onFailure(Call<AccuDailyResult> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[1] = getAccuDaily;
    }

    private void requestHourly(final Context c, final Location location,
                               final WeatherHelper.OnRequestWeatherListener l) {
        Call<List<AccuHourlyResult>> getAccuHourly = buildApi().getHourly(
                location.cityId,
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true);
        getAccuHourly.enqueue(new Callback<List<AccuHourlyResult>>() {
            @Override
            public void onResponse(Call<List<AccuHourlyResult>> call, Response<List<AccuHourlyResult>> response) {
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
            public void onFailure(Call<List<AccuHourlyResult>> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[2] = getAccuHourly;
    }

    private void requestAlert(final Context c, final Location location,
                              final WeatherHelper.OnRequestWeatherListener l) {
        Call<List<AccuAlertResult>> getAccuAlert = buildApi().getAlert(
                location.cityId,
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true);
        getAccuAlert.enqueue(new Callback<List<AccuAlertResult>>() {
            @Override
            public void onResponse(Call<List<AccuAlertResult>> call, Response<List<AccuAlertResult>> response) {
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
            public void onFailure(Call<List<AccuAlertResult>> call, Throwable t) {
                if (l != null) {
                    loadFailed(location, l);
                }
            }
        });
        calls[3] = getAccuAlert;
    }

    private void requestAqi(final Context c, final Location location,
                            final WeatherHelper.OnRequestWeatherListener l) {
        Call<AccuAqiResult> getAccuAqi = buildApi().getAqi(
                location.cityId,
                BuildConfig.ACCU_AQI_KEY);
        getAccuAqi.enqueue(new Callback<AccuAqiResult>() {
            @Override
            public void onResponse(Call<AccuAqiResult> call, Response<AccuAqiResult> response) {
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
            public void onFailure(Call<AccuAqiResult> call, Throwable t) {
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

    public static AccuWeatherService getService() {
        return new AccuWeatherService();
    }

    private AccuWeatherApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.ACCU_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(buildClient())
                .build()
                .create((AccuWeatherApi.class));
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
