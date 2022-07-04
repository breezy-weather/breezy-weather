package wangdaye.com.geometricweather.weather.apis;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.atmoaura.AtmoAuraQAResult;

/**
 * API Atmo AURA
 * Covers Auvergne-Rhône-Alpes
 */

public interface AtmoAuraIqaApi {

    @GET("air2go/v3/point")
    Observable<AtmoAuraQAResult> getPointDetails(@Query("api_token") String api_token,
                                                 @Query("y") String latitude,
                                                 @Query("x") String longitude,
                                                 @Query("datetime_echeance") String datetime_echeance,
                                                 @Query("with_list") boolean with_list);
}