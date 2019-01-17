package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.data.entity.result.accu.AccuDailyResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuRealtimeResult;
import wangdaye.com.geometricweather.data.entity.result.caiyun.CaiYunMainlyResult;
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

    public void buildRealTime(Context c, AccuRealtimeResult result) {
        weather = result.WeatherText;
        weatherKind = WeatherHelper.getAccuWeatherKind(result.WeatherIcon);
        temp = (int) result.Temperature.Metric.Value;
        sensibleTemp = (int) result.RealFeelTemperature.Metric.Value;
        windDir = result.Wind.Direction.Localized;
        windSpeed = WeatherHelper.getWindSpeed(result.Wind.Speed.Metric.Value);
        windLevel = WeatherHelper.getWindLevel(c, result.Wind.Speed.Metric.Value);
        windDegree = result.Wind.Direction.Degrees;
    }

    public void buildRealTime(CNWeatherResult result) {
        weather = result.realtime.weather.info;
        weatherKind = WeatherHelper.getCNWeatherKind(result.realtime.weather.img);
        temp = Integer.parseInt(result.realtime.weather.temperature);
        sensibleTemp = Integer.parseInt(result.realtime.feelslike_c);
        windDir = result.realtime.wind.direct;
        windSpeed = WeatherHelper.getWindSpeed(result.realtime.wind.windspeed);
        windLevel = result.realtime.wind.power;
        windDegree = -1;
    }

    public void buildRealTime(Context context, CaiYunMainlyResult result) {
        weather = WeatherHelper.getCNWeatherName(result.current.weather);
        weatherKind = WeatherHelper.getCNWeatherKind(result.current.weather);
        temp = Integer.parseInt(result.current.temperature.value);
        sensibleTemp = Integer.parseInt(result.current.feelsLike.value);
        windDegree = Integer.parseInt(result.current.wind.direction.value);
        windDir = WeatherHelper.getCNWindName(windDegree);
        windSpeed = WeatherHelper.getWindSpeed(Double.parseDouble(result.current.wind.speed.value));
        windLevel = WeatherHelper.getWindLevel(context, Double.parseDouble(result.current.wind.speed.value));
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
