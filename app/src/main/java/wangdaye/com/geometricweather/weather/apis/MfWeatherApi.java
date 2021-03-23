package wangdaye.com.geometricweather.weather.apis;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.mf.MfCurrentResult;
import wangdaye.com.geometricweather.weather.json.mf.MfEphemerisResult;
import wangdaye.com.geometricweather.weather.json.mf.MfForecastResult;
import wangdaye.com.geometricweather.weather.json.mf.MfForecastV2Result;
import wangdaye.com.geometricweather.weather.json.mf.MfLocationResult;
import wangdaye.com.geometricweather.weather.json.mf.MfRainResult;
import wangdaye.com.geometricweather.weather.json.mf.MfWarningsResult;

/**
 * API Météo France
 */

public interface MfWeatherApi {

    @GET("places")
    Call<List<MfLocationResult>> callWeatherLocation(@Query("q") String q,
                                                     @Query("lat") double lat,
                                                     @Query("lon") double lon,
                                                     @Query("token") String token);

    @GET("places")
    Observable<List<MfLocationResult>> getWeatherLocation(@Query("q") String q,
                                                          @Query("lat") double lat,
                                                          @Query("lon") double lon,
                                                          @Query("token") String token);

    @GET("forecast")
    Observable<MfForecastResult> getForecast(@Query("lat") double lat,
                                             @Query("lon") double lon,
                                             @Query("lang") String lang,
                                             @Query("token") String token);

    @GET("v2/forecast")
    Observable<MfForecastV2Result> getForecastV2(@Query("lat") double lat,
                                                 @Query("lon") double lon,
                                                 @Query("lang") String lang,
                                                 @Query("token") String token);

    @GET("forecast")
    Observable<MfForecastResult> getForecastInstants(@Query("lat") double lat,
                                                     @Query("lon") double lon,
                                                     @Query("lang") String lang,
                                                     @Query("instants") String instants,
                                                     @Query("token") String token);

    @GET("forecast")
    Observable<MfForecastResult> getForecastInseepp(@Query("id") int id,
                                                    @Query("lang") String lang,
                                                    @Query("token") String token);

    @GET("observation/gridded")
    Observable<MfCurrentResult> getCurrent(@Query("lat") double lat,
                                           @Query("lon") double lon,
                                           @Query("lang") String lang,
                                           @Query("token") String token);

    @GET("rain")
    Observable<MfRainResult> getRain(@Query("lat") double lat,
                                     @Query("lon") double lon,
                                     @Query("lang") String lang,
                                     @Query("token") String token);

    @GET("ephemeris")
    Observable<MfEphemerisResult> getEphemeris(@Query("lat") double lat,
                                               @Query("lon") double lon,
                                               @Query("lang") String lang,
                                               @Query("token") String token);

    @GET("warning/full")
    Observable<MfWarningsResult> getWarnings(@Query("domain") String domain,
                                             @Query("formatDate") String formatDate,
                                             @Query("token") String token);
}