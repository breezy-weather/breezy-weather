package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
                if (queries[i].endsWith("省")
                        || queries[i].endsWith("市")
                        || queries[i].endsWith("区")
                        || queries[i].endsWith("县")) {
                    queries[i] = queries[i].substring(0, queries[i].length() - 1);
                }
            }

            if (DatabaseHelper.getInstance(context).countCNCity() < 2567) {
                DatabaseHelper.getInstance(context).writeCityList(FileUtils.readCityList(context));
            }

            List<Location> locationList = new ArrayList<>();
            List<CNCityList.CNCity> cityList;
            CNCityList.CNCity city;
            String query = null;

            if (fuzzy) {
                cityList = DatabaseHelper.getInstance(context).fuzzyReadCNCity(queries[0]);
                if (cityList != null) {
                    query = queries[0];
                    locationList = Location.buildLocationListByCNWeather(cityList);
                }
            } else {
                String[][] cityProvinceSet = null;
                if (queries.length == 3) {
                    cityProvinceSet = new String[][] {
                            new String[] {queries[0], queries[1]},
                            new String[] {queries[0], queries[2]},
                            new String[] {queries[1], queries[2]}};
                } else if (queries.length == 2) {
                    cityProvinceSet = new String[][] {new String[] {queries[0], queries[1]}};
                }
                if (cityProvinceSet != null) {
                    for (String[] cityProvince : cityProvinceSet) {
                        city = DatabaseHelper.getInstance(context).readCNCity(cityProvince[0], cityProvince[1]);
                        if (city != null) {
                            query = cityProvince[0];
                            locationList = Location.buildLocationList(city);
                            break;
                        }
                    }
                } else {
                    city = DatabaseHelper.getInstance(context).readCNCity(queries[0]);
                    if (city != null) {
                        query = queries[0];
                        locationList = Location.buildLocationList(city);
                    }
                }
            }

            if (handler != null) {
                synchronized (handlerLock) {
                    if (handler != null) {
                        final List<Location> finalLocationList = locationList;
                        final String finalQuery = query;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (finalLocationList.size() > 0) {
                                    listener.requestWeatherLocationSuccess(finalQuery, finalLocationList);
                                } else {
                                    listener.requestWeatherLocationFailed(finalQuery);
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
                .client(buildClient())
                .build()
                .create((CNWeatherApi.class));
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
