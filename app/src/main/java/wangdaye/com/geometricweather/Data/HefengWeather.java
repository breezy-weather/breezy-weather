package wangdaye.com.geometricweather.Data;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.Gson;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import wangdaye.com.geometricweather.R;

/**
 * heweather.com
 * */

public class HefengWeather {
    // data
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;

    public static HefengResult requestHourlyData(String location, boolean useEnglish) {
        final String httpUrl = "http://apis.baidu.com/heweather/weather/free";
        final String httpArg = "city=";
        final String APP_KEY_HOURLY = "9c66dd2f8347b7b7b69d4521051a5eb5";

        BufferedReader reader;
        String result;
        String requestCode;
        StringBuffer sbf = new StringBuffer();

        if (useEnglish) {
            String locationEng;
            switch (location) {
                case "厦门":
                    locationEng = "xiamen";
                    break;
                case "蚌埠":
                    locationEng = "bengbu";
                    break;
                case "浚县":
                    locationEng = "xunxian";
                    break;
                case "泌阳":
                    locationEng = "biyang";
                    break;
                case "洪洞":
                    locationEng = "hongtong";
                    break;
                case "六安":
                    locationEng = "luan";
                    break;
                case "黄陂":
                    locationEng = "huangbi";
                    break;
                case "番禺":
                    locationEng = "panyu";
                    break;
                case "香港":
                    locationEng = "hongkang";
                    break;
                case "台北":
                    locationEng = "taipei";
                    break;
                case "澳门":
                    locationEng = "macao";
                    break;
                default:
                    locationEng = charToPinyin(location);
                    break;
            }
            requestCode = httpUrl + "?" + httpArg + locationEng.replaceAll(" ", "");
        } else {
            requestCode = httpUrl + "?" + httpArg + location;
        }

        HefengResult hefengResult = null;
        try {
            URL url = new URL(requestCode);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  APP_KEY_HOURLY);
            connection.setConnectTimeout(DEF_CONN_TIMEOUT);
            connection.setReadTimeout(DEF_READ_TIMEOUT);
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
            String resultExchange = result.replaceFirst("HeWeather data service 3.0", "heWeather");
            //String temp1 = new String("now\": {" + "\"cond");
            //String temp2 = new String("now\": {" + "\"condNow");
            result = resultExchange.replaceAll("now\":\\{\"cond", "now\":{\"condNow");
            hefengResult = new Gson().fromJson(result, HefengResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hefengResult;
    }

    public static HefengResult requestInternationalData(String location) {
        final String httpUrl = "http://apis.baidu.com/heweather/weather/free";
        final String httpArg = "city=";
        final String APP_KEY_INTERNATIONAL = "f8f22c028b3ad53163da5a7a0ca854b3";

        BufferedReader reader;
        String result;
        String requestCode;
        StringBuffer sbf = new StringBuffer();

        requestCode = httpUrl + "?" + httpArg + location.replaceAll(" ", "+");
        HefengResult hefengResult = null;
        try {
            URL url = new URL(requestCode);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  APP_KEY_INTERNATIONAL);
            connection.setConnectTimeout(DEF_CONN_TIMEOUT);
            connection.setReadTimeout(DEF_READ_TIMEOUT);
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
            String resultExchange = result.replaceFirst("HeWeather data service 3.0", "heWeather");
            //String temp1 = new String("now\": { " + "\"cond");
            //String temp2 = new String("now\": { " + "\"condNow");
            result = resultExchange.replaceAll("now\":\\{\"cond", "now\":{\"condNow");
            hefengResult = new Gson().fromJson(result, HefengResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hefengResult;
    }

    public static String charToPinyin(String location) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 不带声调
        format.setVCharType(HanyuPinyinVCharType.WITH_V); // v

        char[] input = location.trim().toCharArray();
        StringBuffer output = new StringBuffer("");

        try {
            for (int i = 0; i < input.length; i++) {
                if (Character.toString(input[i]).matches("[\u4E00-\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    output.append(temp[0]);
                    output.append(" ");
                } else
                    output.append(Character.toString(input[i]));
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public static String getWeatherKind(String weatherCode) {
        int code = Integer.parseInt(weatherCode);
        if (code == 100) {
            return "晴";
        } else if (100 < code && code < 104) {
            return "云";
        } else if (code == 104) {
            return "阴";
        } else if (199 < code && code < 214) {
            return "风";
        } else if (299 < code && code < 302) {
            return "雨";
        } else if (301 < code && code < 304) {
            return "雷雨";
        } else if (code == 304) {
            return "雹";
        } else if (304< code && code < 313) {
            return "雨";
        } else if (code == 313) {
            return "雨夹雪";
        } else if (399 < code && code < 404) {
            return "雪";
        } else if (403 < code && code < 407) {
            return "雨夹雪";
        } else if (code == 407) {
            return "雪";
        } else if (499 < code && code < 502) {
            return "雾";
        } else if (501 < code && code < 509) {
            return "霾";
        } else if (code == 900) {
            return "晴";
        } else if (code == 901) {
            return "雪";
        } else {
            return "阴";
        }
    }

    public static WeatherInfoToShow getWeatherInfoToShow(Context context, HefengResult hefengResult, boolean isDay) {
        if (hefengResult == null) {
            return null;
        } else if (! hefengResult.heWeather.get(0).status.equals("ok")) {
            return null;
        }

        WeatherInfoToShow info = new WeatherInfoToShow();

        int position = 0;
        String updateTime = hefengResult.heWeather.get(0).basic.update.loc;
        for (int i = 1; i < hefengResult.heWeather.size(); i ++) {
            if (hefengResult.heWeather.get(i).basic.update.loc.compareTo(updateTime) > 0) {
                position = i;
                updateTime = hefengResult.heWeather.get(i).basic.update.loc;
            }
        }

        String[] time = hefengResult.heWeather.get(position).basic.update.loc.split(" ");
        info.date = time[0];
        info.moon = "";

        info.refreshTime = time[1];
        info.location = hefengResult.heWeather.get(position).basic.city;

        info.weatherNow = hefengResult.heWeather.get(position).now.condNow.txt;
        info.weatherKindNow = HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).now.condNow.code);
        info.tempNow = hefengResult.heWeather.get(position).now.tmp;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = hefengResult.heWeather.get(position).basic.update.loc.split(" ")[0];
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(todayDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String week = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
        String[] temp = new String[7];
        int day = Integer.parseInt(week);
        for (int i = 0; i < temp.length; i ++) {
            if (day == 1){
                temp[i] = "日";
            } else if (day == 2) {
                temp[i] = "一";
            } else if (day == 3) {
                temp[i] = "二";
            } else if (day == 4) {
                temp[i] = "三";
            } else if (day == 5) {
                temp[i] = "四";
            } else if (day == 6) {
                temp[i] = "五";
            } else if (day == 7) {
                temp[i] = "六";
                day = 0;
            }
            day ++;
        }
        info.week = new String[] {
                context.getString(R.string.week) + temp[0],
                context.getString(R.string.week) + temp[1],
                context.getString(R.string.week) + temp[2],
                context.getString(R.string.week) + temp[3],
                context.getString(R.string.week) + temp[4],
                context.getString(R.string.week) + temp[5],
                context.getString(R.string.week) + temp[6],
        };
        if (isDay) {
            info.weather = new String[] {
                    hefengResult.heWeather.get(position).daily_forecast.get(0).cond.txt_d,
                    hefengResult.heWeather.get(position).daily_forecast.get(1).cond.txt_d,
                    hefengResult.heWeather.get(position).daily_forecast.get(2).cond.txt_d,
                    hefengResult.heWeather.get(position).daily_forecast.get(3).cond.txt_d,
                    hefengResult.heWeather.get(position).daily_forecast.get(4).cond.txt_d,
                    hefengResult.heWeather.get(position).daily_forecast.get(5).cond.txt_d,
                    hefengResult.heWeather.get(position).daily_forecast.get(6).cond.txt_d
            };
            info.weatherKind = new String[] {
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(0).cond.code_d),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(1).cond.code_d),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(2).cond.code_d),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(3).cond.code_d),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(4).cond.code_d),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(5).cond.code_d),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(6).cond.code_d)
            };
        } else {
            info.weather = new String[] {
                    hefengResult.heWeather.get(position).daily_forecast.get(0).cond.txt_n,
                    hefengResult.heWeather.get(position).daily_forecast.get(1).cond.txt_n,
                    hefengResult.heWeather.get(position).daily_forecast.get(2).cond.txt_n,
                    hefengResult.heWeather.get(position).daily_forecast.get(3).cond.txt_n,
                    hefengResult.heWeather.get(position).daily_forecast.get(4).cond.txt_n,
                    hefengResult.heWeather.get(position).daily_forecast.get(5).cond.txt_n,
                    hefengResult.heWeather.get(position).daily_forecast.get(6).cond.txt_n,
            };
            info.weatherKind = new String[] {
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(0).cond.code_n),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(1).cond.code_n),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(2).cond.code_n),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(3).cond.code_n),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(4).cond.code_n),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(5).cond.code_n),
                    HefengWeather.getWeatherKind(hefengResult.heWeather.get(position).daily_forecast.get(6).cond.code_n)
            };
        }
        info.maxiTemp = new String[] {
                hefengResult.heWeather.get(position).daily_forecast.get(0).tmp.max,
                hefengResult.heWeather.get(position).daily_forecast.get(1).tmp.max,
                hefengResult.heWeather.get(position).daily_forecast.get(2).tmp.max,
                hefengResult.heWeather.get(position).daily_forecast.get(3).tmp.max,
                hefengResult.heWeather.get(position).daily_forecast.get(4).tmp.max,
                hefengResult.heWeather.get(position).daily_forecast.get(5).tmp.max,
                hefengResult.heWeather.get(position).daily_forecast.get(6).tmp.max
        };
        info.miniTemp = new String[] {
                hefengResult.heWeather.get(position).daily_forecast.get(0).tmp.min,
                hefengResult.heWeather.get(position).daily_forecast.get(1).tmp.min,
                hefengResult.heWeather.get(position).daily_forecast.get(2).tmp.min,
                hefengResult.heWeather.get(position).daily_forecast.get(3).tmp.min,
                hefengResult.heWeather.get(position).daily_forecast.get(4).tmp.min,
                hefengResult.heWeather.get(position).daily_forecast.get(5).tmp.min,
                hefengResult.heWeather.get(position).daily_forecast.get(6).tmp.min
        };

        info.windTitle = hefengResult.heWeather.get(position).daily_forecast.get(0).wind.dir
                + "(" + context.getString(R.string.live) + hefengResult.heWeather.get(position).now.wind.dir + ")";
        info.windInfo = hefengResult.heWeather.get(position).daily_forecast.get(0).wind.sc + "级"
                + "(" + hefengResult.heWeather.get(position).now.wind.sc + "级" + ")";
        info.pmTitle = context.getString(R.string.visibility);
        info.pmInfo = hefengResult.heWeather.get(position).now.vis + "km";
        info.humTitle = "相对" + context.getString(R.string.humidity);
        info.humInfo = hefengResult.heWeather.get(position).now.hum + "%";
        info.uvTitle = context.getString(R.string.sun_rise) + "-" + hefengResult.heWeather.get(position).daily_forecast.get(0).astro.sr;
        info.uvInfo = context.getString(R.string.sun_fall) + "-" + hefengResult.heWeather.get(position).daily_forecast.get(0).astro.ss;
        info.dressTitle = context.getString(R.string.apparent_temp);
        info.dressInfo = hefengResult.heWeather.get(position).now.fl + "℃";
        info.coldTitle = context.getString(R.string.cold_index);
        info.coldInfo = "无数据";
        info.airTitle = context.getString(R.string.air_index);
        info.airInfo = "无数据";
        info.washCarTitle = context.getString(R.string.wash_car_index);
        info.washCarInfo = "无数据";
        info.exerciseTitle = context.getString(R.string.exercise_index);
        info.exerciseInfo = "无数据";

        return info;
    }
}