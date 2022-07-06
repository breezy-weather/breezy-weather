package wangdaye.com.geometricweather.basic.deprecated;
/*
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.basic.deprecated.FWApi;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.network.result.old.FWResult;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
*/
/**
 * Flyme weather.
 * */
/*
public class FWeather {
    // widget
    private Call call;
*/
    /** <br> data. */
/*
    public FWeather requestFWeather(final Location location,
                                    final WeatherHelper.OnRequestWeatherListener l) {
        Call<FWResult> getFWeather = buildApi().getFWeather(location.getCityId());
        getFWeather.enqueue(new Callback<FWResult>() {
            @Override
            public void onResponse(Call<FWResult> call, Response<FWResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestWeatherSuccess(Weather.buildWeather(response.body()), location);
                    } else {
                        l.requestWeatherFailed(location, false);
                    }
                }
            }

            @Override
            public void onFailure(Call<FWResult> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherFailed(location, false);
                }
            }
        });
        call = getFWeather;
        return this;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    public static FWeather getService() {
        return new FWeather();
    }

    private FWApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.FW_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((FWApi.class));
    }
}
*/