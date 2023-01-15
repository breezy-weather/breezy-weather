package wangdaye.com.geometricweather.weather.apis;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.nominatim.NominatimLocationResult;

/**
 * Nominatim API.
 */

public interface NominatimApi {

    @GET("search")
    Call<List<NominatimLocationResult>> callWeatherLocation(@Header("User-Agent") String userAgent,
                                                            @Query("q") String q,
                                                            @Query("featuretype") String featureType,
                                                            @Query("addressdetails") Boolean addressDetails,
                                                            @Query("accept-language") String acceptLanguage,
                                                            @Query("format") String format);

    @GET("search")
    Observable<List<NominatimLocationResult>> getWeatherLocation(@Header("User-Agent") String userAgent,
                                                                 @Query("q") String q,
                                                                 @Query("featuretype") String featureType,
                                                                 @Query("addressdetails") Boolean addressDetails,
                                                                 @Query("accept-language") String acceptLanguage,
                                                                 @Query("format") String format);

    @GET("search")
    Observable<NominatimLocationResult> getWeatherLocationByGeoPosition(@Header("User-Agent") String userAgent,
                                                                        @Query("lat") Float lat,
                                                                        @Query("lon") Float lon,
                                                                        @Query("featuretype") String featureType,
                                                                        @Query("addressdetails") Boolean addressDetails,
                                                                        @Query("accept-language") String acceptLanguage,
                                                                        @Query("format") String format);

}