package wangdaye.com.geometricweather.Data;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by WangDaYe on 2016/2/21.
 */

public class HefengWeather {
    // data
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;

    public static HefengResult request(String location) {
        final String httpUrl = "http://apis.baidu.com/heweather/weather/free";
        final String httpArg = "city=";
        final String APP_KEY = "9c66dd2f8347b7b7b69d4521051a5eb5";

        BufferedReader reader = null;
        String result;
        StringBuffer sbf = new StringBuffer();
        String requestCode = httpUrl + "?" + httpArg + location;
        HefengResult hefengResult = null;
        try {
            URL url = new URL(requestCode);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  APP_KEY);
            connection.setConnectTimeout(DEF_CONN_TIMEOUT);
            connection.setReadTimeout(DEF_READ_TIMEOUT);
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
            String resultExchange = result.replaceFirst("HeWeather data service 3.0", "heWeather");
            hefengResult = new Gson().fromJson(resultExchange, HefengResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hefengResult;
    }
}
