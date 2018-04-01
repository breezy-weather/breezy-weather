package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

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
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * CN weather service.
 * */

public class CNWeatherService {

    private Handler handler;
    private final Object handlerLock = new Object();

    private Call call;

    private class SearchCityRunnable implements Runnable {

        private Context context;
        private String[] queries;
        private boolean fuzzy;
        private LocationHelper.OnRequestWeatherLocationListener listener;

        SearchCityRunnable(Context context, String[] queries, boolean fuzzy,
                           LocationHelper.OnRequestWeatherLocationListener l) {
            this.context = context;
            this.queries = queries;
            this.fuzzy = fuzzy;
            this.listener = l;
        }

        @Override
        public void run() {
            if (listener == null) {
                return;
            }

            for (int i = 0; i < queries.length; i ++) {
                if (queries[i].endsWith("市") || queries[i].endsWith("区") || queries[i].endsWith("县")) {
                    queries[i] = queries[i].substring(0, queries[i].length() - 1);
                }
            }

            if (DatabaseHelper.getInstance(context).countCNCity() < 2567) {
                DatabaseHelper.getInstance(context).writeCityList(FileUtils.readCityList(context));
            }

            final List<Location> list = new ArrayList<>();
            CNCityList.CNCity city;
            String query = null;
            for (String q : queries) {
                if (fuzzy) {
                    city = DatabaseHelper.getInstance(context).fuzzyReadCNCity(q);
                } else {
                    city = DatabaseHelper.getInstance(context).readCNCity(q);
                }
                if (city != null) {
                    query = q;
                    Location location = new Location();
                    location.cityId = city.id;
                    location.city = city.name;
                    location.prov = city.province_name;
                    location.cnty = "中国";
                    location.local = false;
                    list.add(location);
                    break;
                }
            }
            if (handler != null) {
                synchronized (handlerLock) {
                    if (handler != null) {
                        final String finalQuery = query;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (list.size() > 0) {
                                    listener.requestWeatherLocationSuccess(finalQuery, list);
                                } else {
                                    listener.requestWeatherLocationFailed(queries[0]);
                                }
                                handler = null;
                            }
                        });
                    }
                }
            }
        }
    }

    void requestWeather(final Context context, final Location location,
                        final WeatherHelper.OnRequestWeatherListener l) {
        Call<CNWeatherResult> getWeather = buildApi().getWeather(location.cityId);
        getWeather.enqueue(new Callback<CNWeatherResult>() {
            @Override
            public void onResponse(Call<CNWeatherResult> call, Response<CNWeatherResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        Weather weather = new Weather();
                        weather.base.buildBase(location, response.body());
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
                            weather.alertList.add(new Alert().buildAlert(context, response.body().alert.get(i)));
                        }
                        l.requestWeatherSuccess(weather, location);
                    } else {
                        l.requestWeatherFailed(location);
                    }
                }
            }

            @Override
            public void onFailure(Call<CNWeatherResult> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherFailed(location);
                }
            }
        });
        call = getWeather;
    }

    void requestLocation(Context context, String[] queries, boolean fuzzy,
                         LocationHelper.OnRequestWeatherLocationListener l) {
        handler = new Handler();
        ThreadManager.getInstance().execute(new SearchCityRunnable(context, queries, fuzzy, l));
    }

    public void cancel() {
        if (handler != null) {
            synchronized (handlerLock) {
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                    handler = null;
                }
            }
        }
        if (call != null) {
            call.cancel();
        }
    }

    public static CNWeatherService getService() {
        return new CNWeatherService();
    }

    private CNWeatherApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.CN_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((CNWeatherApi.class));
    }
}
