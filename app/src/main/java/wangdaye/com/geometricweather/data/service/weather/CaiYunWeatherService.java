package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.data.api.CaiYunApi;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Alert;
import wangdaye.com.geometricweather.data.entity.model.weather.Daily;
import wangdaye.com.geometricweather.data.entity.model.weather.Hourly;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.result.caiyun.CaiYunForecastResult;
import wangdaye.com.geometricweather.data.entity.result.caiyun.CaiYunMainlyResult;
import wangdaye.com.geometricweather.utils.GzipInterceptor;

/**
 * CaiYun weather service.
 * */

public class CaiYunWeatherService extends CNWeatherService {

    private Call mainlyCall;
    private Call forecastCall;

    private Location requestLocation;
    private CaiYunMainlyResult mainlyResult;
    private CaiYunForecastResult forecastResult;

    private int successTime;

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        this.requestLocation = location;
        this.successTime = 0;
        this.mainlyResult = null;
        this.forecastResult = null;
        requestMainlyWeather(context, location, callback);
        requestForecastWeather(context, location, callback);
    }

    private void requestMainlyWeather(final Context context,
                                      final Location location, @NonNull final RequestWeatherCallback callback) {
        Call<CaiYunMainlyResult> getMainlyWeather = buildApi().getMainlyWeather(
                location.lat,
                location.lon,
                location.isLocal(),
                "weathercn%3A" + location.cityId,
                15,
                "weather20151024",
                "zUFJoAR2ZVrDy1vF3D07",
                "V10.0.1.0.OAACNFH",
                "10010002",
                false,
                false,
                "gemini",
                "",
                "zh_cn");
        getMainlyWeather.enqueue(new Callback<CaiYunMainlyResult>() {
            @Override
            public void onResponse(Call<CaiYunMainlyResult> call, Response<CaiYunMainlyResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!isFailed(location)) {
                        mainlyResult = response.body();
                        loadSucceed(context, location, callback);
                    }
                } else {
                    loadFailed(location, callback);
                }
            }

            @Override
            public void onFailure(Call<CaiYunMainlyResult> call, Throwable t) {
                callback.requestWeatherFailed(location);
            }
        });
        mainlyCall = getMainlyWeather;
    }

    private void requestForecastWeather(final Context context,
                                        final Location location, @NonNull final RequestWeatherCallback callback) {
        Call<CaiYunForecastResult> getForecastWeather = buildApi().getForecastWeather(
                location.lat,
                location.lon,
                "zh_cn",
                false,
                "weather20151024",
                "weathercn%3A" + location.cityId,
                "zUFJoAR2ZVrDy1vF3D07");
        getForecastWeather.enqueue(new Callback<CaiYunForecastResult>() {
            @Override
            public void onResponse(Call<CaiYunForecastResult> call, Response<CaiYunForecastResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!isFailed(location)) {
                        forecastResult = response.body();
                        loadSucceed(context, location, callback);
                    }
                } else {
                    loadFailed(location, callback);
                }
            }

            @Override
            public void onFailure(Call<CaiYunForecastResult> call, Throwable t) {
                callback.requestWeatherFailed(location);
            }
        });
        forecastCall = getForecastWeather;
    }

    @Override
    public void cancel() {
        super.cancel();
        if (mainlyCall != null) {
            mainlyCall.cancel();
        }
        if (forecastCall != null) {
            forecastCall.cancel();
        }
    }

    private boolean isSucceed(Location location) {
        return successTime == 2 && location.equals(requestLocation);
    }

    private boolean isFailed(Location location) {
        return successTime < 0 || !location.equals(requestLocation);
    }

    private void loadSucceed(Context context, Location location, RequestWeatherCallback callback) {
        successTime ++;
        if (isSucceed(location)) {
            try {
                Weather weather = new Weather();
                weather.base.buildBase(location, mainlyResult);
                weather.realTime.buildRealTime(context, mainlyResult);
                for (int i = 0; i < mainlyResult.forecastDaily.weather.value.size(); i ++) {
                    weather.dailyList.add(new Daily().buildDaily(context, mainlyResult, i));
                }

                location.history = new History();
                location.history.cityId = location.cityId;
                location.history.city = location.city;
                location.history.date = mainlyResult.yesterday.date.split("T")[0];
                location.history.maxiTemp = Integer.parseInt(mainlyResult.yesterday.tempMax);
                location.history.miniTemp = Integer.parseInt(mainlyResult.yesterday.tempMin);

                for (int i = 0; i < mainlyResult.forecastHourly.weather.value.size(); i ++) {
                    weather.hourlyList.add(new Hourly().buildHourly(context, mainlyResult, i));
                }
                weather.aqi.buildAqi(context, mainlyResult);
                weather.index.buildIndex(context, mainlyResult, forecastResult);
                for (int i = 0; i < mainlyResult.alerts.size(); i ++) {
                    weather.alertList.add(new Alert().buildAlert(context, mainlyResult.alerts.get(i)));
                }
                callback.requestWeatherSuccess(weather, requestLocation);
            } catch (Exception e) {
                callback.requestWeatherFailed(requestLocation);
            }
        }
    }

    private void loadFailed(Location location, RequestWeatherCallback callback) {
        if (!isFailed(location)) {
            successTime = -1;
            callback.requestWeatherFailed(requestLocation);
        }
    }

    private CaiYunApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.CAIYUN_WEATHER_BASE_URL)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                new GsonBuilder().setLenient().create()))
                .client(buildClient())
                .build()
                .create((CaiYunApi.class));
    }

    private OkHttpClient buildClient() {
        return getClientBuilder()
                .addInterceptor(new GzipInterceptor())
                // .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }
}
