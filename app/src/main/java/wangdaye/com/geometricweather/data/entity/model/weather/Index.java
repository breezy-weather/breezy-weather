package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuDailyResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuMinuteResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuRealtimeResult;
import wangdaye.com.geometricweather.data.entity.result.caiyun.CaiYunForecastResult;
import wangdaye.com.geometricweather.data.entity.result.caiyun.CaiYunMainlyResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Index.
 * */

public class Index {

    public String simpleForecast;
    public String briefing;
    public String currentWind;
    public String dailyWind;
    public String sensibleTemp;
    public String humidity;
    public String uv;
    public String pressure;
    public String visibility;
    public String dewPoint;

    Index() {
        simpleForecast = "";
        briefing = "";
        currentWind = "";
        dailyWind = "";
        sensibleTemp = "";
        humidity = "";
        uv = "";
        pressure = "";
        visibility = "";
        dewPoint = "";
    }

    public void buildIndex(Context c, AccuRealtimeResult result) {
        currentWind = c.getString(R.string.live) + " : " + result.Wind.Direction.Localized
                + " " + WeatherHelper.getWindSpeed(result.Wind.Speed.Metric.Value)
                + " (" + WeatherHelper.getWindLevel(c, result.Wind.Speed.Metric.Value) + ") "
                + WeatherHelper.getWindArrows(result.Wind.Direction.Degrees);
        sensibleTemp = c.getString(R.string.sensible_temp) + " : " + ValueUtils.buildCurrentTemp(
                (int) result.RealFeelTemperature.Metric.Value, false, GeometricWeather.getInstance().isFahrenheit());
        humidity = c.getString(R.string.humidity) + " : " + result.RelativeHumidity + "%";
        uv = result.UVIndex + " / " + result.UVIndexText;
        if (GeometricWeather.getInstance().isImperial()) {
            pressure = result.Pressure.Imperial.Value + result.Pressure.Imperial.Unit;
        } else {
            pressure = result.Pressure.Metric.Value + result.Pressure.Metric.Unit;
        }
        if (GeometricWeather.getInstance().isImperial()) {
            visibility = result.Visibility.Imperial.Value + result.Visibility.Imperial.Unit;
        } else {
            visibility = result.Visibility.Metric.Value + result.Visibility.Metric.Unit;
        }
        dewPoint = ValueUtils.buildCurrentTemp(
                (int) result.DewPoint.Metric.Value, false, GeometricWeather.getInstance().isFahrenheit());
    }

    public void buildIndex(Context c, AccuDailyResult result) {
        simpleForecast = result.Headline.Text;
        dailyWind = c.getString(R.string.daytime) + " : " + result.DailyForecasts.get(0).Day.Wind.Direction.Localized
                + " " + WeatherHelper.getWindSpeed(result.DailyForecasts.get(0).Day.Wind.Speed.Value)
                + " (" + WeatherHelper.getWindLevel(c, result.DailyForecasts.get(0).Day.Wind.Speed.Value) + ") "
                + WeatherHelper.getWindArrows(result.DailyForecasts.get(0).Day.Wind.Direction.Degrees) + "\n"
                + c.getString(R.string.nighttime) + " : " + result.DailyForecasts.get(0).Night.Wind.Direction.Localized
                + " " + WeatherHelper.getWindSpeed(result.DailyForecasts.get(0).Night.Wind.Speed.Value)
                + " (" + WeatherHelper.getWindLevel(c, result.DailyForecasts.get(0).Night.Wind.Speed.Value) + ") "
                + WeatherHelper.getWindArrows(result.DailyForecasts.get(0).Night.Wind.Direction.Degrees);
    }

    public void buildIndex(AccuMinuteResult result) {
        briefing = result.getSummary().getLongPhrase();
    }

    public void buildIndex(Context c, CNWeatherResult result) {
        simpleForecast = "";
        briefing = result.life.info.daisan.get(1);
        currentWind = c.getString(R.string.live) + " : " + result.realtime.wind.direct
                + " " + WeatherHelper.getWindSpeed(result.realtime.wind.windspeed)
                + " (" + result.realtime.wind.power + ")";
        dailyWind = c.getString(R.string.daytime) + " : " + result.weather.get(0).info.day.get(3)
                + " " + WeatherHelper.getWindSpeed(result.weather.get(0).info.day.get(4)) + "\n"
                + c.getString(R.string.nighttime) + " : " + result.weather.get(0).info.night.get(3)
                + " " + WeatherHelper.getWindSpeed(result.weather.get(0).info.night.get(4));
        sensibleTemp = c.getString(R.string.sensible_temp) + " : " + result.realtime.feelslike_c + "℃";
        humidity = c.getString(R.string.humidity) + " : " + result.realtime.weather.humidity;
        uv = result.life.info.ziwaixian.get(0) + "。" + result.life.info.ziwaixian.get(1);
        pressure = result.realtime.pressure + "hPa";
        visibility = "";
        dewPoint = "";
    }

    public void buildIndex(Context c, CaiYunMainlyResult mainly, CaiYunForecastResult forecast) {
        simpleForecast = "";
        briefing = forecast.precipitation.description;
        currentWind = c.getString(R.string.live) + " : "
                + WeatherHelper.getCNWindName(Integer.parseInt(mainly.current.wind.direction.value))
                + " " + WeatherHelper.getWindSpeed(mainly.current.wind.speed.value)
                + " (" + WeatherHelper.getWindLevel(c, Double.parseDouble(mainly.current.wind.speed.value)) + ")";
        dailyWind = c.getString(R.string.daytime) + " : "
                + WeatherHelper.getCNWindName(Integer.parseInt(mainly.forecastDaily.wind.direction.value.get(0).from))
                + " " + WeatherHelper.getWindSpeed(mainly.forecastDaily.wind.speed.value.get(0).from)
                + " (" + WeatherHelper.getWindLevel(c, Double.parseDouble(mainly.forecastDaily.wind.speed.value.get(0).from)) + ")" + "\n"
                + c.getString(R.string.nighttime) + " : "
                + WeatherHelper.getCNWindName(Integer.parseInt(mainly.forecastDaily.wind.direction.value.get(0).to))
                + " " + WeatherHelper.getWindSpeed(mainly.forecastDaily.wind.speed.value.get(0).to)
                + " (" + WeatherHelper.getWindLevel(c, Double.parseDouble(mainly.forecastDaily.wind.speed.value.get(0).to)) + ")";
        sensibleTemp = c.getString(R.string.sensible_temp) + " : " + mainly.current.feelsLike.value + "℃";
        humidity = c.getString(R.string.humidity) + " : " + mainly.current.humidity.value;
        uv = WeatherHelper.getCNUVIndex(mainly.current.uvIndex);
        pressure = mainly.current.pressure.value + mainly.current.pressure.unit;
        visibility = "";
        dewPoint = "";
    }

    void buildIndex(WeatherEntity entity) {
        simpleForecast = entity.indexSimpleForecast;
        briefing = entity.indexBriefing;
        currentWind = entity.indexCurrentWind;
        dailyWind = entity.indexDailyWind;
        sensibleTemp = entity.indexSensibleTemp;
        humidity = entity.indexHumidity;
        uv = entity.indexUv;
        pressure = entity.indexPressure;
        visibility = entity.indexVisibility;
        dewPoint = entity.indexDewPoint;
    }
}