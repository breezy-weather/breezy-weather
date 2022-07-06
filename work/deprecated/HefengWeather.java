package wangdaye.com.geometricweather.basic.deprecated;
/*
import android.annotation.SuppressLint;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.basic.deprecated.HefengApi;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.network.result.old.HefengResult;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
*/
/**
 * Hefeng realTimeWeather.
 * */
/*
public class HefengWeather {
    // widget
    private Call call;
*/
    /** <br> data. */
/*
    public HefengWeather requestHefengWeather(final Weather oldResult,
                                              final Location location,
                                              final WeatherHelper.OnRequestWeatherListener l) {
        Call<HefengResult> getHefengWeather = buildApi().getHefengWeather(location.city);
        getHefengWeather.enqueue(new Callback<HefengResult>() {
            @Override
            public void onResponse(Call<HefengResult> call, Response<HefengResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestWeatherSuccess(Weather.buildWeather(oldResult, response.body()), location);
                    } else {
                        l.requestWeatherFailed(location, false);
                    }
                }
            }

            @Override
            public void onFailure(Call<HefengResult> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherFailed(location, false);
                }
            }
        });
        call = getHefengWeather;
        return this;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    public static HefengWeather getService() {
        return new HefengWeather();
    }

    private HefengApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(HefengApi.BASE_URL)
                .client(buildClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((HefengApi.class));
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("apikey", HefengApi.APP_KEY)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();
    }
*/
    /** <br> utils. */
/*
    public static int getLatestDataPosition(HefengResult result) {
        if (result == null
                || result.heWeather == null || result.heWeather.size() == 0
                || !result.heWeather.get(0).status.equals("ok")) {
            return 0;
        }
        int position = 0;
        String updateTime = result.heWeather.get(0).basic.update.loc;
        for (int i = 1; i < result.heWeather.size(); i ++) {
            if (result.heWeather.get(i).basic.update.loc.compareTo(updateTime) > 0) {
                position = i;
                updateTime = result.heWeather.get(i).basic.update.loc;
            }
        }
        return position;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getWeek(String dateTxt, boolean eng) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(dateTxt));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == 1){
            return eng ? "SUN" : "周日";
        } else if (day == 2) {
            return eng ? "MON" : "周一";
        } else if (day == 3) {
            return eng ? "TUE" : "周二";
        } else if (day == 4) {
            return eng ? "WED" : "周三";
        } else if (day == 5) {
            return eng ? "THU" : "周四";
        } else if (day == 6) {
            return eng ? "FRI" : "周五";
        } else {
            return eng ? "SAT" : "周六";
        }
    }
}*/