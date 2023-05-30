package wangdaye.com.geometricweather.weather.apis;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.mf.MfCurrentResult;
import wangdaye.com.geometricweather.weather.json.mf.MfEphemerisResult;
import wangdaye.com.geometricweather.weather.json.mf.MfForecastV2Result;
import wangdaye.com.geometricweather.weather.json.mf.MfLocationResult;
import wangdaye.com.geometricweather.weather.json.mf.MfRainResult;
import wangdaye.com.geometricweather.weather.json.mf.MfWarningsResult;

/**
 * API Météo France
 */

public interface MfWeatherApi {

    @GET("places")
    Call<List<MfLocationResult>> callWeatherLocation(@Header("User-Agent") String userAgent,
                                                     @Query("q") String q,
                                                     @Query("lat") double lat,
                                                     @Query("lon") double lon,
                                                     @Query("token") String token);

    @GET("places")
    Observable<List<MfLocationResult>> getWeatherLocation(@Header("User-Agent") String userAgent,
                                                          @Query("q") String q,
                                                          @Query("lat") double lat,
                                                          @Query("lon") double lon,
                                                          @Query("token") String token);

    @GET("v2/forecast")
    Observable<MfForecastV2Result> getForecastV2(@Header("User-Agent") String userAgent,
                                                 @Query("lat") double lat,
                                                 @Query("lon") double lon,
                                                 @Query("formatDate") String formatDate,
                                                 @Query(encoded = true, value = "instants") String instants,
                                                 @Query("token") String token);

    @GET("v2/observation")
    Observable<MfCurrentResult> getCurrent(@Header("User-Agent") String userAgent,
                                           @Query("lat") double lat,
                                           @Query("lon") double lon,
                                           @Query("lang") String lang,
                                           @Query("formatDate") String formatDate,
                                           @Query("token") String token);

    @GET("v3/nowcast/rain")
    Observable<MfRainResult> getRain(@Header("User-Agent") String userAgent,
                                     @Query("lat") double lat,
                                     @Query("lon") double lon,
                                     @Query("lang") String lang,
                                     @Query("formatDate") String formatDate,
                                     @Query("token") String token);

    @GET("ephemeris")
    Observable<MfEphemerisResult> getEphemeris(@Header("User-Agent") String userAgent,
                                               @Query("lat") double lat,
                                               @Query("lon") double lon,
                                               @Query("lang") String lang,
                                               @Query("formatDate") String formatDate,
                                               @Query("token") String token);

    @GET("v2/warning/full")
    Observable<MfWarningsResult> getWarnings(@Header("User-Agent") String userAgent,
                                             @Query(encoded = true, value = "domain") String domain,
                                             @Query("formatDate") String formatDate,
                                             @Query("token") String token);
}