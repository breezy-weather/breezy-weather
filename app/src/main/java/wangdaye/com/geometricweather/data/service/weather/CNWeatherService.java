package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.data.api.CNWeatherApi;
import wangdaye.com.geometricweather.data.entity.model.CNCityList;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Alert;
import wangdaye.com.geometricweather.data.entity.model.weather.Daily;
import wangdaye.com.geometricweather.data.entity.model.weather.Hourly;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.utils.FileUtils;
import wangdaye.com.geometricweather.utils.GzipInterceptor;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * CN weather service.
 * */

public class CNWeatherService extends WeatherService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Call call;

    @Override
    public void requestWeather(final Context context,
                               final Location location, @NonNull final RequestWeatherCallback callback) {
        Call<CNWeatherResult> getWeather = buildApi().getWeather(location.cityId);
        getWeather.enqueue(new Callback<CNWeatherResult>() {
            @Override
            public void onResponse(Call<CNWeatherResult> call, Response<CNWeatherResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Weather weather = new Weather();
                    weather.base.buildBase(context, location, response.body());
                    weather.realTime.buildRealTime(response.body());
                    while (response.body().weather.size() > 0
                            && TimeManager.compareDate(response.body().weather.get(0).date, response.body().realtime.date) < 0) {
                        if (response.body().weather.size() > 1
                                && TimeManager.compareDate(response.body().weather.get(1).date, response.body().realtime.date) == 0) {
                            location.history = new History();
                            location.history.cityId = location.cityId;
                            location.history.city = location.city;
                            location.history.date = response.body().weather.get(0).date;
                            location.history.maxiTemp = Integer.parseInt(response.body().weather.get(0).info.day.get(2));
                            location.history.miniTemp = Integer.parseInt(response.body().weather.get(0).info.night.get(2));
                        }
                        response.body().weather.remove(0);
                    }
                    for (int i = 0; i < response.body().weather.size(); i ++) {
                        weather.dailyList.add(new Daily().buildDaily(context, response.body().weather.get(i)));
                    }
                    for (int i = 0; i < response.body().hourly_forecast.size(); i ++) {
                        weather.hourlyList.add(
                                new Hourly()
                                        .buildHourly(
                                                context,
                                                response.body().weather.get(0),
                                                response.body().hourly_forecast.get(i)));
                    }
                    weather.aqi.buildAqi(context, response.body());
                    weather.index.buildIndex(context, response.body());
                    for (int i = 0; i < response.body().alert.size(); i ++) {
                        for (int j = i + 1; j < response.body().alert.size(); j ++) {
                            if (response.body().alert.get(i).alarmTp1.equals(response.body().alert.get(j).alarmTp1)
                                    && response.body().alert.get(i).alarmTp2.equals(response.body().alert.get(j).alarmTp2)) {
                                response.body().alert.remove(j);
                                j --;
                            }
                        }
                    }
                    for (int i = 0; i < response.body().alert.size(); i ++) {
                        weather.alertList.add(new Alert().buildAlert(context, response.body().alert.get(i)));
                    }
                    callback.requestWeatherSuccess(weather, location);
                } else {
                    callback.requestWeatherFailed(location);
                }
            }

            @Override
            public void onFailure(Call<CNWeatherResult> call, Throwable t) {
                callback.requestWeatherFailed(location);
            }
        });
        call = getWeather;
    }

    @Override
    public void requestLocation(Context context, String query, @NonNull RequestLocationCallback callback) {
        searchInThread(context, new String[] {query}, true, callback);
    }

    @Override
    public void requestLocation(Context context, String[] queries, @NonNull RequestLocationCallback callback) {
        searchInThread(context, queries, false, callback);
    }

    @Override
    public void requestLocation(Context context, String lat, String lon, @NonNull RequestLocationCallback callback) {
        // do nothing.
    }

    @Override
    public void cancel() {
        handler.removeCallbacksAndMessages(null);
        if (call != null) {
            call.cancel();
        }
    }

    private void searchInThread(final Context context, final String[] queries, final boolean fuzzy,
                                final RequestLocationCallback callback) {
        ThreadManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (callback == null) {
                    return;
                }

                if (DatabaseHelper.getInstance(context).countCNCity() < 2971) {
                    DatabaseHelper.getInstance(context).writeCityList(FileUtils.readCityList(context));
                }

                List<Location> locationList = new ArrayList<>();
                List<CNCityList.CNCity> cityList;
                CNCityList.CNCity city;

                if (fuzzy) {
                    cityList = DatabaseHelper.getInstance(context).fuzzyReadCNCity(queries[0]);
                    if (cityList != null) {
                        locationList = Location.buildLocationListByCNWeather(cityList);
                    }
                } else {
                    if (queries.length == 3) {
                        city = DatabaseHelper.getInstance(context).readCNCity(queries[0], queries[1], queries[2]);
                        if (city != null) {
                            locationList = Location.buildLocationList(city);
                        }
                    } else {
                        city = DatabaseHelper.getInstance(context).readCNCity(queries[0]);
                        if (city != null) {
                            locationList = Location.buildLocationList(city);
                        }
                    }
                }

                final List<Location> finalLocationList = locationList;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalLocationList.size() > 0) {
                            callback.requestLocationSuccess(queries[0], finalLocationList);
                        } else {
                            callback.requestLocationFailed(queries[0]);
                        }
                    }
                });
            }
        });
    }

    private CNWeatherApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.CN_WEATHER_BASE_URL)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                new GsonBuilder().setLenient().create()))
                .client(buildClient())
                .build()
                .create((CNWeatherApi.class));
    }

    private OkHttpClient buildClient() {
        return getClientBuilder()
                .addInterceptor(new GzipInterceptor())
                // .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }
}
