package wangdaye.com.geometricweather.data.service;

import android.content.Context;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.data.api.JuheApi;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.data.entity.result.JuheResult;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Juhe weather.
 * */

public class JuheWeather {
    // widget
    private Call call;

    /** <br> data. */

    public JuheWeather requestJuheWeather(final Context c, final Location location,
                                          final WeatherHelper.OnRequestWeatherListener l) {
        Call<JuheResult> getJuheWeather = buildApi().getJuheWeather(location.realName, JuheApi.APP_KEY);
        getJuheWeather.enqueue(new Callback<JuheResult>() {
            @Override
            public void onResponse(Call<JuheResult> call, Response<JuheResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestWeatherSuccess(Weather.build(c, response.body()), location.name);
                    } else {
                        l.requestWeatherFailed(location.name);
                    }
                }
            }

            @Override
            public void onFailure(Call<JuheResult> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherFailed(location.name);
                }
            }
        });
        call = getJuheWeather;
        return this;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    public static JuheWeather getService() {
        return new JuheWeather();
    }

    private JuheApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(JuheApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((JuheApi.class));
    }
}
