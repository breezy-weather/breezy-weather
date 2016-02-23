package wangdaye.com.geometricweather.Data;

import android.util.Log;

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
        final String APP_KEY_1 = "9c66dd2f8347b7b7b69d4521051a5eb5";
//        final String APP_KEY_2 = "f8f22c028b3ad53163da5a7a0ca854b3";

        BufferedReader reader = null;
        String result;
        StringBuffer sbf = new StringBuffer();
        String locationPinyin = charToPinyin(location);
        String requestCode = httpUrl + "?" + httpArg + locationPinyin.replaceAll(" ", "");
        HefengResult hefengResult = null;
        try {
            URL url = new URL(requestCode);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  APP_KEY_1);
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
}
