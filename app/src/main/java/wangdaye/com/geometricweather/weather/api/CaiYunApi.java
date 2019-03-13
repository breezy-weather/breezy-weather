package wangdaye.com.geometricweather.weather.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunForecastResult;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunMainlyResult;

public interface CaiYunApi {

    @GET("wtr-v3/weather/all")
    Observable<CaiYunMainlyResult> getMainlyWeather(@Query("latitude") String latitude,
                                                    @Query("longitude") String longitude,
                                                    @Query("isLocated") boolean isLocated,
                                                    @Query("locationKey") String locationKey,
                                                    @Query("days") int days,
                                                    @Query("appKey") String appKey,
                                                    @Query("sign") String sign,
                                                    @Query("romVersion") String romVersion,
                                                    @Query("appVersion") String appVersion,
                                                    @Query("alpha") boolean alpha,
                                                    @Query("isGlobal") boolean isGlobal,
                                                    @Query("device") String device,
                                                    @Query("modDevice") String modDevice,
                                                    @Query("locale") String locale);

    @GET("wtr-v3/weather/xm/forecast/minutely")
    Observable<CaiYunForecastResult> getForecastWeather(@Query("latitude") String latitude,
                                                        @Query("longitude") String longitude,
                                                        @Query("locale") String locale,
                                                        @Query("isGlobal") boolean isGlobal,
                                                        @Query("appKey") String appKey,
                                                        @Query("locationKey") String locationKey,
                                                        @Query("sign") String sign);
}
