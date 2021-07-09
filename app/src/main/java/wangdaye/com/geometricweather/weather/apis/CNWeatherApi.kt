package wangdaye.com.geometricweather.weather.apis

import retrofit2.http.GET
import retrofit2.http.Path
import wangdaye.com.geometricweather.weather.json.cn.CNWeatherResult

/**
 * CN weather api.
 */
interface CNWeatherApi {

    @GET("v4/{city_id}.json")
    suspend fun getWeather(@Path("city_id") city_id: String): CNWeatherResult
}