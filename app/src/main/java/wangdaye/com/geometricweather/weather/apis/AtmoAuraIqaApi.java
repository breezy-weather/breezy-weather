package wangdaye.com.geometricweather.weather.apis;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.atmoaura.AtmoAuraPointResult;

/**
 * API Atmo AURA
 * Covers Auvergne-Rhône-Alpes
 */

public interface AtmoAuraIqaApi {

    @GET("air2go/v3/point?with_list=true")
    Observable<AtmoAuraPointResult> getPointDetails(@Query("api_token") String api_token,
                                                    @Query("x") String longitude,
                                                    @Query("y") String latitude,
                                                    @Query("datetime_echeance") String datetime_echeance);
}