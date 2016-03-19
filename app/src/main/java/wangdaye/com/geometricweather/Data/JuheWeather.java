package wangdaye.com.geometricweather.Data;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import wangdaye.com.geometricweather.R;

/**
 * juhe.cn
 * */

public class JuheWeather {
    // data
    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static String userAgent =  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

    public static final String APPKEY ="5bf9785af8c13ea44ab55442d63bc0ad";

    public static JuheResult getRequest(String city) {
        String result;
        String url = "http://op.juhe.cn/onebox/weather/query";//请求接口地址
        Map<String, Object> params = new HashMap<>();//请求参数
        params.put("cityname", city);//要查询的城市，如：温州、上海、北京
        params.put("key", APPKEY);//应用APPKEY(应用详细页查询)
        params.put("dtype", "");//返回数据的格式,xml或json，默认json
        JuheResult juheResult = null; // 用于接收结果的泛型类
        try {
            result = net(url, params, "GET");
            String resultExchange = result.replaceFirst("weather", "weatherNow");
            result = resultExchange.replaceFirst("info", "weatherInfo");
            resultExchange = result.replaceFirst("info", "lifeInfo");
            result = resultExchange.replaceFirst("pm25", "air");
            Gson gson = new Gson();
            juheResult = gson.fromJson(result, JuheResult.class);
            if (juheResult.error_code.equals("0")) {
                Log.i("JuheWeather", "聚合天气" + city + " ：成功");
            } else {
                Log.i("JuheWeather", "聚合天气 ：" + juheResult.error_code + juheResult.reason);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return juheResult;
    }

// weather

    /**
     *
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return  网络请求字符串
     * @throws Exception
     */
    public static String net(String strUrl, Map<String, Object> params,String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuilder sb = new StringBuilder();
            if(method==null || method.equals("GET")){
                strUrl = strUrl+"?"+urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if(method==null || method.equals("GET")){
                conn.setRequestMethod("GET");
            }else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params!= null && method.equals("POST")) {
                try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                    out.writeBytes(urlencode(params));
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String, ?> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ?> i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

// data

    public static String getWeatherKind(String weatherInfo) {
        if(weatherInfo.contains("雨")) {
            if(weatherInfo.contains("雪")) {
                return "雨夹雪";
            } else if(weatherInfo.contains("雷")) {
                return "雷雨";
            } else {
                return "雨";
            }
        }
        if(weatherInfo.contains("雷")) {
            if (weatherInfo.contains("雨")) {
                return "雷雨";
            } else {
                return "雷";
            }
        }
        if (weatherInfo.contains("雪")) {
            if(weatherInfo.contains("雨")) {
                return "雨夹雪";
            } else {
                return "雪";
            }
        }
        if (weatherInfo.contains("雹")) {
            return "冰雹";
        }
        if (weatherInfo.contains("冰")) {
            return "冰雹";
        }
        if (weatherInfo.contains("冻")) {
            return "冰雹";
        }
        if (weatherInfo.contains("云")) {
            return "云";
        }
        if (weatherInfo.contains("阴")) {
            return "阴";
        }
        if (weatherInfo.contains("风")) {
            return "风";
        }
        if(weatherInfo.contains("沙")) {
            return "霾";
        }
        if(weatherInfo.contains("尘")) {
            return "霾";
        }
        if(weatherInfo.contains("雾")) {
            return "雾";
        }
        if(weatherInfo.contains("霾")) {
            return "霾";
        }
        if (weatherInfo.contains("晴")) {
            return "晴";
        }
        return "阴";
    }

    public static int[] getWeatherIcon(String weatherKind, boolean isDay) {
        int[] imageId = new int[4];

        switch (weatherKind) {
            case "晴":
                if(isDay) {
                    imageId[0] = R.drawable.weather_sun_day_1;
                    imageId[1] = R.drawable.weather_sun_day_2;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_sun_day;
                } else {
                    imageId[0] = R.drawable.weather_sun_night;
                    imageId[1] = 0;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_sun_night;
                }
                break;
            case "云":
                if(isDay) {
                    imageId[0] = R.drawable.weather_cloud_day_1;
                    imageId[1] = R.drawable.weather_cloud_day_2;
                    imageId[2] = R.drawable.weather_cloud_day_3;
                    imageId[3] = R.drawable.weather_cloud_day;
                } else {
                    imageId[0] = R.drawable.weather_cloud_night_1;
                    imageId[1] = R.drawable.weather_cloud_night_2;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_cloud_night;
                }
                break;
            case "阴":
                imageId[0] = R.drawable.weather_cloudy_1;
                imageId[1] = R.drawable.weather_cloudy_2;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_cloudy;
                break;
            case "雨":
                imageId[0] = R.drawable.weather_rain_1;
                imageId[1] = R.drawable.weather_rain_2;
                imageId[2] = R.drawable.weather_rain_3;
                imageId[3] = R.drawable.weather_rain;
                break;
            case "风":
                imageId[0] = R.drawable.weather_wind;
                imageId[1] = 0;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_wind;
                break;
            case "雪":
                imageId[0] = R.drawable.weather_snow_1;
                imageId[1] = R.drawable.weather_snow_2;
                imageId[2] = R.drawable.weather_snow_3;
                imageId[3] = R.drawable.weather_snow;
                break;
            case "雾":
                imageId[0] = R.drawable.weather_fog;
                imageId[1] = R.drawable.weather_fog;
                imageId[2] = R.drawable.weather_fog;
                imageId[3] = R.drawable.weather_fog;
                break;
            case "霾":
                imageId[0] = R.drawable.weather_haze_1;
                imageId[1] = R.drawable.weather_haze_2;
                imageId[2] = R.drawable.weather_haze_3;
                imageId[3] = R.drawable.weather_haze;
                break;
            case "雨夹雪":
                imageId[0] = R.drawable.weather_sleet_1;
                imageId[1] = R.drawable.weather_sleet_2;
                imageId[2] = R.drawable.weather_sleet_3;
                imageId[3] = R.drawable.weather_sleet;
                break;
            case "雷雨":
                imageId[0] = R.drawable.weather_thunderstorm_1;
                imageId[1] = R.drawable.weather_thunderstorm_2;
                imageId[2] = R.drawable.weather_thunderstorm_3;
                imageId[3] = R.drawable.weather_thunderstorm;
                break;
            case "雷":
                imageId[0] = R.drawable.weather_thunder_1;
                imageId[1] = R.drawable.weather_thunder_2;
                imageId[2] = R.drawable.weather_thunder_2;
                imageId[3] = R.drawable.weather_thunder;
                break;
            case "冰雹":
                imageId[0] = R.drawable.weather_hail_1;
                imageId[1] = R.drawable.weather_hail_2;
                imageId[2] = R.drawable.weather_hail_3;
                imageId[3] = R.drawable.weather_hail;
                break;
            default:
                imageId[0] = R.drawable.weather_cloudy_1;
                imageId[1] = R.drawable.weather_cloudy_2;
                imageId[2] = R.drawable.weather_cloudy_2;
                imageId[3] = R.drawable.weather_cloudy;
                break;
        }
        return imageId;
    }

    public static int[] getAnimatorId(String weatherKind, boolean isDay) {
        int[] animatorId = new int[3];

        switch (weatherKind) {
            case "晴":
                if(isDay) {
                    animatorId[0] = R.animator.weather_sun_day_1;
                    animatorId[1] = R.animator.weather_sun_day_2;
                    animatorId[2] = 0;
                } else {
                    animatorId[0] = R.animator.weather_sun_night;
                    animatorId[1] = 0;
                    animatorId[2] = 0;
                }
                break;
            case "云":
                if(isDay) {
                    animatorId[0] = R.animator.weather_cloud_day_1;
                    animatorId[1] = R.animator.weather_cloud_day_2;
                    animatorId[2] = R.animator.weather_cloud_day_3;
                } else {
                    animatorId[0] = R.animator.weather_cloud_night_1;
                    animatorId[1] = R.animator.weather_cloud_night_2;
                    animatorId[2] = 0;
                }
                break;
            case "阴":
                animatorId[0] = R.animator.weather_cloudy_1;
                animatorId[1] = R.animator.weather_cloudy_2;
                animatorId[2] = 0;
                break;
            case "雨":
                animatorId[0] = R.animator.weather_rain_1;
                animatorId[1] = R.animator.weather_rain_2;
                animatorId[2] = R.animator.weather_rain_3;
                break;
            case "风":
                animatorId[0] = R.animator.weather_wind;
                animatorId[1] = 0;
                animatorId[2] = 0;
                break;
            case "雪":
                animatorId[0] = R.animator.weather_snow_1;
                animatorId[1] = R.animator.weather_snow_2;
                animatorId[2] = R.animator.weather_snow_3;
                break;
            case "雾":
                animatorId[0] = R.animator.weather_fog_1;
                animatorId[1] = R.animator.weather_fog_2;
                animatorId[2] = R.animator.weather_fog_3;
                break;
            case "霾":
                animatorId[0] = R.animator.weather_haze_1;
                animatorId[1] = R.animator.weather_haze_2;
                animatorId[2] = R.animator.weather_haze_3;
                break;
            case "雨夹雪":
                animatorId[0] = R.animator.weather_sleet_1;
                animatorId[1] = R.animator.weather_sleet_2;
                animatorId[2] = R.animator.weather_sleet_3;
                break;
            case "雷雨":
                animatorId[0] = R.animator.weather_thunderstorm_1;
                animatorId[1] = R.animator.weather_thunderstorm_2;
                animatorId[2] = R.animator.weather_thunderstorm_3;
                break;
            case "雷":
                animatorId[0] = R.animator.weather_thunder_1;
                animatorId[1] = R.animator.weather_thunder_2;
                animatorId[2] = R.animator.weather_thunder_2;
                break;
            case "冰雹":
                animatorId[0] = R.animator.weather_hail_1;
                animatorId[1] = R.animator.weather_hail_2;
                animatorId[2] = R.animator.weather_hail_3;
                break;
            default:
                animatorId[0] = R.animator.weather_cloudy_1;
                animatorId[1] = R.animator.weather_cloudy_2;
                animatorId[2] = 0;
                break;
        }
        return animatorId;
    }

    public static int getMiniWeatherIcon(String weatherInfo, boolean isDay) {
        int imageId;
        switch (weatherInfo) {
            case "晴":
                if(isDay) {
                    imageId = R.drawable.weather_sun_day_mini;
                } else {
                    imageId = R.drawable.weather_sun_night_mini;
                }
                break;
            case "云":
                if(isDay) {
                    imageId = R.drawable.weather_cloud_day_mini;
                } else {
                    imageId = R.drawable.weather_cloud_mini;
                }
                break;
            case "阴":
                imageId = R.drawable.weather_cloud_mini;
                break;
            case "雨":
                imageId = R.drawable.weather_rain_mini;
                break;
            case "风":
                imageId = R.drawable.weather_wind_mini;
                break;
            case "雪":
                imageId = R.drawable.weather_snow_mini;
                break;
            case "雾":
                imageId = R.drawable.weather_fog_mini;
                break;
            case "霾":
                imageId = R.drawable.weather_haze_mini;
                break;
            case "雨夹雪":
                imageId = R.drawable.weather_sleet_mini;
                break;
            case "雷雨":
                imageId = R.drawable.weather_thunder_mini;
                break;
            case "雷":
                imageId = R.drawable.weather_thunder_mini;
                break;
            case "冰雹":
                imageId = R.drawable.weather_hail_mini;
                break;
            default:
                if(isDay) {
                    imageId = R.drawable.weather_cloud_day_mini;
                } else {
                    imageId = R.drawable.weather_cloud_mini;
                }
                break;
        }
        return imageId;
    }

    public static WeatherInfoToShow getWeatherInfoToShow(Context context, JuheResult juheResult, boolean isDay) {
        if (juheResult == null) {
            return null;
        } else if (! juheResult.error_code.equals("0")) {
            return null;
        }

        WeatherInfoToShow info = new WeatherInfoToShow();

        info.date = juheResult.result.data.realtime.date;
        info.moon = " / " + juheResult.result.data.realtime.moon;

        String[] time = juheResult.result.data.realtime.time.split(":");
        info.refreshTime = time[0] + ":" + time[1];

        info.location = juheResult.result.data.realtime.city_name;

        info.weatherNow = juheResult.result.data.realtime.weatherNow.weatherInfo;
        info.weatherKindNow = JuheWeather.getWeatherKind(info.weatherNow);
        info.tempNow = juheResult.result.data.realtime.weatherNow.temperature;

        info.week = new String[] {
                context.getString(R.string.week) + juheResult.result.data.weather.get(0).week,
                context.getString(R.string.week) + juheResult.result.data.weather.get(1).week,
                context.getString(R.string.week) + juheResult.result.data.weather.get(2).week,
                context.getString(R.string.week) + juheResult.result.data.weather.get(3).week,
                context.getString(R.string.week) + juheResult.result.data.weather.get(4).week,
                context.getString(R.string.week) + juheResult.result.data.weather.get(5).week,
                context.getString(R.string.week) + juheResult.result.data.weather.get(6).week
        };
        if (isDay) {
            info.weather = new String[] {
                    juheResult.result.data.weather.get(0).info.day.get(1),
                    juheResult.result.data.weather.get(1).info.day.get(1),
                    juheResult.result.data.weather.get(2).info.day.get(1),
                    juheResult.result.data.weather.get(3).info.day.get(1),
                    juheResult.result.data.weather.get(4).info.day.get(1),
                    juheResult.result.data.weather.get(5).info.day.get(1),
                    juheResult.result.data.weather.get(6).info.day.get(1)
            };
        } else {
            info.weather = new String[] {
                    juheResult.result.data.weather.get(0).info.night.get(1),
                    juheResult.result.data.weather.get(1).info.night.get(1),
                    juheResult.result.data.weather.get(2).info.night.get(1),
                    juheResult.result.data.weather.get(3).info.night.get(1),
                    juheResult.result.data.weather.get(4).info.night.get(1),
                    juheResult.result.data.weather.get(5).info.night.get(1),
                    juheResult.result.data.weather.get(6).info.night.get(1)
            };
        }
        info.weatherKind = new String[] {
                JuheWeather.getWeatherKind(info.weather[0]),
                JuheWeather.getWeatherKind(info.weather[1]),
                JuheWeather.getWeatherKind(info.weather[2]),
                JuheWeather.getWeatherKind(info.weather[3]),
                JuheWeather.getWeatherKind(info.weather[4]),
                JuheWeather.getWeatherKind(info.weather[5]),
                JuheWeather.getWeatherKind(info.weather[6])
        };

        if (isDay) {
            info.windDir = new String[7];
            for (int i = 0; i < 7; i ++) {
                info.windDir[i] = juheResult.result.data.weather.get(i).info.day.get(3);
            }
            info.windLevel = new String[7];
            for (int i = 0; i < 7; i ++) {
                info.windLevel[i] = juheResult.result.data.weather.get(i).info.day.get(4);
            }
        } else {
            info.windDir = new String[7];
            for (int i = 0; i < 7; i ++) {
                info.windDir[i] = juheResult.result.data.weather.get(i).info.night.get(3);
            }
            info.windLevel = new String[7];
            for (int i = 0; i < 7; i ++) {
                info.windLevel[i] = juheResult.result.data.weather.get(i).info.night.get(4);
            }
        }

        info.maxiTemp = new String[] {
                juheResult.result.data.weather.get(0).info.day.get(2),
                juheResult.result.data.weather.get(1).info.day.get(2),
                juheResult.result.data.weather.get(2).info.day.get(2),
                juheResult.result.data.weather.get(3).info.day.get(2),
                juheResult.result.data.weather.get(4).info.day.get(2),
                juheResult.result.data.weather.get(5).info.day.get(2),
                juheResult.result.data.weather.get(6).info.day.get(2)
        };
        info.miniTemp = new String[] {
                juheResult.result.data.weather.get(0).info.night.get(2),
                juheResult.result.data.weather.get(1).info.night.get(2),
                juheResult.result.data.weather.get(2).info.night.get(2),
                juheResult.result.data.weather.get(3).info.night.get(2),
                juheResult.result.data.weather.get(4).info.night.get(2),
                juheResult.result.data.weather.get(5).info.night.get(2),
                juheResult.result.data.weather.get(6).info.night.get(2)
        };

        info.windTitle = juheResult.result.data.weather.get(0).info.day.get(3)
                + "(" + context.getString(R.string.live) + juheResult.result.data.realtime.wind.direct + ")";
        info.windInfo = juheResult.result.data.weather.get(0).info.day.get(4)
                + "(" + context.getString(R.string.live) + juheResult.result.data.realtime.wind.power + ")";
        info.pmTitle = context.getString(R.string.pm_25) + " : " + juheResult.result.data.air.pm25.pm25
                + " , " + context.getString(R.string.pm_10) + " : " + juheResult.result.data.air.pm25.pm10;
        info.pmInfo = context.getString(R.string.pm_level) + ":" +  juheResult.result.data.air.pm25.quality;
        info.humTitle = context.getString(R.string.humidity);
        info.humInfo = juheResult.result.data.realtime.weatherNow.humidity;
        info.uvTitle = context.getString(R.string.uv) + "-" + juheResult.result.data.life.lifeInfo.ziwaixian.get(0);
        info.uvInfo = juheResult.result.data.life.lifeInfo.ziwaixian.get(1);
        info.dressTitle = context.getString(R.string.dressing_index) + "-" + juheResult.result.data.life.lifeInfo.chuanyi.get(0);
        info.dressInfo = juheResult.result.data.life.lifeInfo.chuanyi.get(1);
        info.coldTitle = context.getString(R.string.cold_index) + "-" + juheResult.result.data.life.lifeInfo.chuanyi.get(0);
        info.coldInfo = juheResult.result.data.life.lifeInfo.ganmao.get(1);
        info.airTitle = context.getString(R.string.air_index) + "-" + juheResult.result.data.life.lifeInfo.wuran.get(0);
        info.airInfo = juheResult.result.data.life.lifeInfo.wuran.get(1);
        info.washCarTitle = context.getString(R.string.wash_car_index) + "-" + juheResult.result.data.life.lifeInfo.xiche.get(0);
        info.washCarInfo = juheResult.result.data.life.lifeInfo.xiche.get(1);
        info.exerciseTitle = context.getString(R.string.exercise_index) + "-" + juheResult.result.data.life.lifeInfo.yundong.get(0);
        info.exerciseInfo = juheResult.result.data.life.lifeInfo.yundong.get(1);

        return info;
    }
}
