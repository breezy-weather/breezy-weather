package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.data.entity.result.accu.AccuDailyResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuRealtimeResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Real time.
 * */

public class RealTime {

    public String weather;
    public String weatherKind;
    public int temp;
    public int sensibleTemp;
    public String windDir;
    public String windSpeed;
    public String windLevel;
    public int windDegree;
    public String simpleForecast;

    RealTime() {}

/*
    void buildRealTime(FWResult result) {
        weather = result.realtime.weather;
        weatherKind = WeatherHelper.getFWeatherKind(weather);
        temp = Integer.parseInt(result.realtime.temp.replace("°", ""));
        sensibleTemp = Integer.parseInt(result.realtime.sendibleTemp.replace("°", ""));
        windDir = result.realtime.wD;
        windSpeed = "";
        windLevel = result.realtime.wS;
        simpleForecast = "";
    }

    void buildRealTime(HefengResult result, int p) {
        weather = result.heWeather.get(p).now.cond.txt;
        weatherKind = WeatherHelper.getHefengWeatherKind(result.heWeather.get(p).now.cond.code);
        temp = Integer.parseInt(result.heWeather.get(p).now.tmp);
        sensibleTemp = Integer.parseInt(result.heWeather.get(p).now.fl);
        windDir = result.heWeather.get(p).now.wind.dir;
        windSpeed = result.heWeather.get(p).now.wind.spd + "km/h";
        windLevel = "#" + result.heWeather.get(p).now.wind.sc;
        simpleForecast = "";
    }
*/

    public void buildRealTime(Context c, AccuRealtimeResult result) {
        weather = result.WeatherText;
        weatherKind = WeatherHelper.getNewWeatherKind(result.WeatherIcon);
        temp = (int) result.Temperature.Metric.Value;
        sensibleTemp = (int) result.RealFeelTemperature.Metric.Value;
        windDir = result.Wind.Direction.Localized;
        windSpeed = WeatherHelper.getWindSpeed(result.Wind.Speed.Metric.Value);
        windLevel = WeatherHelper.getWindLevel(c, result.Wind.Speed.Metric.Value);
        windDegree = result.Wind.Direction.Degrees;
    }

    public void buildRealTime(CNWeatherResult result) {
        weather = result.realtime.weather.info;
        weatherKind = WeatherHelper.getNewWeatherKind(result.realtime.weather.img);
        temp = Integer.parseInt(result.realtime.weather.temperature);
        sensibleTemp = Integer.parseInt(result.realtime.feelslike_c);
        windDir = result.realtime.wind.direct;
        windSpeed = WeatherHelper.getWindSpeed(result.realtime.wind.windspeed);
        windLevel = result.realtime.wind.power;
        windDegree = -1;
    }

    public void buildRealTime(AccuDailyResult result) {
        simpleForecast = result.Headline.Text;
    }

    void buildRealTime(WeatherEntity entity) {
        weather = entity.realTimeWeather;
        weatherKind = entity.realTimeWeatherKind;
        temp = entity.realTimeTemp;
        sensibleTemp = entity.realTimeSensibleTemp;
        windDir = entity.realTimeWindDir;
        try {
            windSpeed = WeatherHelper.getWindSpeed(entity.realTimeWindSpeed);
        } catch (Exception e) {
            windSpeed = entity.realTimeWindSpeed;
        }
        windLevel = entity.realTimeWindLevel;
        windDegree = entity.realTimeWindDegree;
        simpleForecast = entity.realTimeSimpleForecast;
    }
}
