package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.data.entity.result.neww.NewDailyResult;
import wangdaye.com.geometricweather.data.entity.result.neww.NewRealtimeResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Real time.
 * */

public class RealTime {
    // data
    public String weather;
    public String weatherKind;
    public int temp;
    public int sensibleTemp;
    public String windDir;
    public String windSpeed;
    public String windLevel;
    public String simpleForecast;

    /** <br> life cycle. */

    RealTime() {
    }
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
    public void buildRealTime(Context c, NewRealtimeResult result) {
        weather = result.WeatherText;
        weatherKind = WeatherHelper.getNewWeatherKind(result.WeatherIcon);
        temp = (int) result.Temperature.Metric.Value;
        sensibleTemp = (int) result.RealFeelTemperature.Metric.Value;
        windDir = result.Wind.Direction.Localized;
        windSpeed = result.Wind.Speed.Metric.Value + "km/h";
        windLevel = WeatherHelper.getWindLevel(c, result.Wind.Speed.Metric.Value);
    }

    public void buildRealTime(NewDailyResult result) {
        simpleForecast = result.Headline.Text;
    }

    void buildRealTime(WeatherEntity entity) {
        weather = entity.realTimeWeather;
        weatherKind = entity.realTimeWeatherKind;
        temp = entity.realTimeTemp;
        sensibleTemp = entity.realTimeSensibleTemp;
        windDir = entity.realTimeWindDir;
        windSpeed = entity.realTimeWindSpeed;
        windLevel = entity.realTimeWindLevel;
        simpleForecast = entity.realTimeSimpleForecast;
    }
}
