package wangdaye.com.geometricweather.basic.deprecated;
/*
import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import wangdaye.com.geometricweather.data.entity.result.cityList.CityListResult;
import wangdaye.com.geometricweather.data.entity.result.cityList.OverseaCityListResult;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.view.activity.MainActivity;
*/
/**
 * File utils.
 * */
/*
public class FileUtils {
*/
    /** <br> city list. */
/*
    public static void writeCityList(Context context, SafeHandler handler) {
        String stringResult = readAssetFileToString(context, "cityList.txt");
        handler.obtainMessage(MainActivity.MESSAGE_WHAT_WRITING_CITY, 1).sendToTarget();
        CityListResult result = new Gson().fromJson(stringResult, CityListResult.class);
        handler.obtainMessage(MainActivity.MESSAGE_WHAT_WRITING_CITY, 2).sendToTarget();
        DatabaseHelper.getInstance(context).writeCityList(result);
        handler.obtainMessage(MainActivity.MESSAGE_WHAT_WRITING_CITY, 3).sendToTarget();
    }

    public static void writeOverseaCityList(Context context, SafeHandler handler) {
        String stringResult = readAssetFileToString(context, "overseaCityList.txt");
        handler.obtainMessage(MainActivity.MESSAGE_WHAT_WRITING_CITY, 4).sendToTarget();
        OverseaCityListResult result = new Gson().fromJson(stringResult, OverseaCityListResult.class);
        handler.obtainMessage(MainActivity.MESSAGE_WHAT_WRITING_CITY, 5).sendToTarget();
        DatabaseHelper.getInstance(context).writeOverseaCityList(result);
        handler.obtainMessage(MainActivity.MESSAGE_WHAT_WRITING_CITY, 6).sendToTarget();
    }

    private static String readAssetFileToString(Context context, String fileName) {
        String result = "";
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            bufReader = new BufferedReader(inputReader);
            String line;

            while ((line = bufReader.readLine()) != null) result += line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeIO(inputReader, bufReader);

        return result;
    }

    private static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/