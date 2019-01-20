package wangdaye.com.geometricweather.data.service.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.data.api.AccuWeatherApi;
import wangdaye.com.geometricweather.data.entity.model.History;
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
import wangdaye.com.geometricweather.data.entity.result.accu.AccuMinuteResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuRealtimeResult;
import wangdaye.com.geometricweather.utils.GzipInterceptor;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Accu weather service.
 * */

public class AccuWeatherService extends WeatherService {

    private AccuWeatherApi api;

    private Call[] weatherCalls = new Call[6];
    private Call locationCall;

    private Weather weather;
    private History history;
    private Location requestLocation;

    private int successTime;

    private String languageCode = "en";

    public AccuWeatherService() {
        OkHttpClient client = getClientBuilder()
                .addInterceptor(new GzipInterceptor())
                // .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        client.dispatcher().setMaxRequestsPerHost(1);

        this.api = new Retrofit.Builder()
                .baseUrl(BuildConfig.ACCU_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().setLenient().create()))
                .client(client)
                .build()
                .create((AccuWeatherApi.class));
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        this.weather = new Weather();
        this.history = null;
        this.requestLocation = location;
        this.successTime = 0;
        this.languageCode = LanguageUtils.getLanguageCode(context);

        requestRealtime(context, location, callback);
        requestDaily(context, location, callback);
        requestHourly(context, location, callback);
        requestMinute(location, callback);
        requestAlert(context, location, callback);
        requestAqi(context, location, callback);
    }

    @Override
    public void requestLocation(Context context, final String query,
                                @NonNull final RequestLocationCallback callback) {
        this.languageCode = LanguageUtils.getLanguageCode(context);
        Call<List<AccuLocationResult>> getAccuLocation = api.getWeatherLocation(
                "Always",
                BuildConfig.ACCU_WEATHER_KEY,
                query,
                languageCode);
        getAccuLocation.enqueue(new Callback<List<AccuLocationResult>>() {
            @Override
            public void onResponse(Call<List<AccuLocationResult>> call, Response<List<AccuLocationResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.requestLocationSuccess(query, Location.buildLocationListByAccuResult(response.body()));
                } else {
                    callback.requestLocationFailed(query);
                }
            }

            @Override
            public void onFailure(Call<List<AccuLocationResult>> call, Throwable t) {
                callback.requestLocationFailed(query);
            }
        });
        locationCall = getAccuLocation;
    }

    @Override
    public void requestLocation(Context context, String[] queries, @NonNull RequestLocationCallback callback) {
        if (queries != null && !TextUtils.isEmpty(queries[0])) {
            requestLocation(context, queries[0], callback);
        }
    }

    @Override
    public void requestLocation(Context context, final String lat, final String lon,
                                @NonNull final RequestLocationCallback callback) {
        this.languageCode = LanguageUtils.getLanguageCode(context);
        Call<AccuLocationResult> getAccuLocationByGeoPosition = api.getWeatherLocationByGeoPosition(
                "Always",
                BuildConfig.ACCU_WEATHER_KEY,
                lat + "," + lon,
                languageCode);
        getAccuLocationByGeoPosition.enqueue(new Callback<AccuLocationResult>() {
            @Override
            public void onResponse(Call<AccuLocationResult> call, Response<AccuLocationResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.requestLocationSuccess(lat + ", " + lon, Location.buildLocationList(response.body()));
                } else {
                    callback.requestLocationFailed(lat + ", " + lon);
                }
            }

            @Override
            public void onFailure(Call<AccuLocationResult> call, Throwable t) {
                callback.requestLocationFailed(lat + ", " + lon);
            }
        });
        locationCall = getAccuLocationByGeoPosition;
    }

    @Override
    public void cancel() {
        if (locationCall != null) {
            locationCall.cancel();
        }
        for (Call call : weatherCalls) {
            if (call != null) {
                call.cancel();
            }
        }
    }

    private boolean isSucceed(Location location) {
        return successTime == 6 && location.equals(requestLocation);
    }

    private boolean isFailed(Location location) {
        return successTime < 0 || !location.equals(requestLocation);
    }

    private void loadSucceed(Location location, RequestWeatherCallback callback) {
        successTime ++;
        if (isSucceed(location)) {
            callback.requestWeatherSuccess(weather, history, requestLocation);
        }
    }

    private void loadFailed(Location location, RequestWeatherCallback callback) {
        if (!isFailed(location)) {
            successTime = -1;
            callback.requestWeatherFailed(requestLocation);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void requestRealtime(final Context c, final Location location,
                                 final RequestWeatherCallback callback) {
        Call<List<AccuRealtimeResult>> getAccuRealtime = api.getRealtime(
                location.cityId,
                BuildConfig.ACCU_CURRENT_KEY,
                languageCode,
                true);
        getAccuRealtime.enqueue(new Callback<List<AccuRealtimeResult>>() {
            @Override
            public void onResponse(Call<List<AccuRealtimeResult>> call, Response<List<AccuRealtimeResult>> response) {
                if (callback != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            weather.base.buildBase(c, location, response.body().get(0));
                            weather.realTime.buildRealTime(c, response.body().get(0));
                            weather.index.buildIndex(c, response.body().get(0));

                            try {
                                history = new History();
                                history.cityId = location.cityId;
                                history.city = weather.base.city;

                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = format.parse(weather.base.date);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                calendar.add(Calendar.DATE, -1);

                                history.date = format.format(calendar.getTime());
                                history.maxiTemp = (int) response.body().get(0)
                                        .TemperatureSummary.Past24HourRange.Maximum.Metric.Value;
                                history.miniTemp = (int) response.body().get(0)
                                        .TemperatureSummary.Past24HourRange.Minimum.Metric.Value;
                            } catch (Exception e) {
                                history = null;
                            }
                            loadSucceed(location, callback);
                        }
                    } else {
                        loadFailed(location, callback);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AccuRealtimeResult>> call, Throwable t) {
                if (callback != null) {
                    loadFailed(location, callback);
                }
            }
        });
        weatherCalls[0] = getAccuRealtime;
    }

    private void requestDaily(final Context c, final Location location,
                              final RequestWeatherCallback callback) {
        Call<AccuDailyResult> getAccuDaily = api.getDaily(
                location.cityId,
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true,
                true);
        getAccuDaily.enqueue(new Callback<AccuDailyResult>() {
            @Override
            public void onResponse(Call<AccuDailyResult> call, Response<AccuDailyResult> response) {
                if (callback != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            weather.realTime.buildRealTime(response.body());
                            for (int i = 0; i < response.body().DailyForecasts.size(); i ++) {
                                weather.dailyList.add(new Daily().buildDaily(c, response.body().DailyForecasts.get(i)));
                            }
                            weather.index.buildIndex(c, response.body());
                            loadSucceed(location, callback);
                        }
                    } else {
                        loadFailed(location, callback);
                    }
                }
            }

            @Override
            public void onFailure(Call<AccuDailyResult> call, Throwable t) {
                if (callback != null) {
                    loadFailed(location, callback);
                }
            }
        });
        weatherCalls[1] = getAccuDaily;
    }

    private void requestHourly(final Context c, final Location location,
                               final RequestWeatherCallback callback) {
        Call<List<AccuHourlyResult>> getAccuHourly = api.getHourly(
                location.cityId,
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true);
        getAccuHourly.enqueue(new Callback<List<AccuHourlyResult>>() {
            @Override
            public void onResponse(Call<List<AccuHourlyResult>> call, Response<List<AccuHourlyResult>> response) {
                if (callback != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            for (int i = 0; i < response.body().size(); i ++) {
                                weather.hourlyList.add(new Hourly().buildHourly(c, response.body().get(i)));
                            }
                            loadSucceed(location, callback);
                        }
                    } else {
                        loadFailed(location, callback);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AccuHourlyResult>> call, Throwable t) {
                if (callback != null) {
                    loadFailed(location, callback);
                }
            }
        });
        weatherCalls[2] = getAccuHourly;
    }

    private void requestMinute(final Location location,
                               final RequestWeatherCallback callback) {
        Call<AccuMinuteResult> getAccuMinute = api.getMinute(
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true,
                location.lat + "," + location.lon);
        getAccuMinute.enqueue(new Callback<AccuMinuteResult>() {
            @Override
            public void onResponse(Call<AccuMinuteResult> call, Response<AccuMinuteResult> response) {
                if (callback != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            weather.index.buildIndex(response.body());

                        }
                    }
                    loadSucceed(location, callback);
                }
            }

            @Override
            public void onFailure(Call<AccuMinuteResult> call, Throwable t) {
                if (callback != null) {
                    loadSucceed(location, callback);
                }
            }
        });
        weatherCalls[3] = getAccuMinute;
    }

    private void requestAlert(final Context c, final Location location,
                              final RequestWeatherCallback callback) {
        Call<List<AccuAlertResult>> getAccuAlert = api.getAlert(
                location.cityId,
                BuildConfig.ACCU_WEATHER_KEY,
                languageCode,
                true);
        getAccuAlert.enqueue(new Callback<List<AccuAlertResult>>() {
            @Override
            public void onResponse(Call<List<AccuAlertResult>> call, Response<List<AccuAlertResult>> response) {
                if (callback != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isFailed(location)) {
                            for (int i = 0; i < response.body().size(); i ++) {
                                weather.alertList.add(new Alert().buildAlert(c, response.body().get(i)));
                            }
                            loadSucceed(location, callback);
                        }
                    } else {
                        loadFailed(location, callback);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AccuAlertResult>> call, Throwable t) {
                if (callback != null) {
                    loadFailed(location, callback);
                }
            }
        });
        weatherCalls[4] = getAccuAlert;
    }

    private void requestAqi(final Context c, final Location location,
                            final RequestWeatherCallback callback) {
        Call<AccuAqiResult> getAccuAqi = api.getAqi(
                location.cityId,
                BuildConfig.ACCU_AQI_KEY);
        getAccuAqi.enqueue(new Callback<AccuAqiResult>() {
            @Override
            public void onResponse(Call<AccuAqiResult> call, Response<AccuAqiResult> response) {
                if (callback != null) {
                    if (response.isSuccessful()) {
                        if (!isFailed(location)) {
                            weather.aqi.buildAqi(c, response.body());
                            loadSucceed(location, callback);
                        }
                    } else {
                        loadFailed(location, callback);
                    }
                }
            }

            @Override
            public void onFailure(Call<AccuAqiResult> call, Throwable t) {
                if (callback != null) {
                    loadFailed(location, callback);
                }
            }
        });
        weatherCalls[5] = getAccuAqi;
    }
}