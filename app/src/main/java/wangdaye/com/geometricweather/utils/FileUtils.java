package wangdaye.com.geometricweather.utils;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import wangdaye.com.geometricweather.data.entity.result.CityListResult;
import wangdaye.com.geometricweather.data.entity.result.OverseaCityListResult;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;

/**
 * File utils.
 * */

public class FileUtils {

    /** <br> city list. */

    public static void writeCityList(Context context) {
        String stringResult = readAssetFileToString(context, "cityList.txt");
        CityListResult result = new Gson().fromJson(stringResult, CityListResult.class);
        DatabaseHelper.getInstance(context).writeCityList(result);
    }

    public static void writeOverseaCityList(Context context) {
        String stringResult = readAssetFileToString(context, "overseaCityList.txt");
        OverseaCityListResult result = new Gson().fromJson(stringResult, OverseaCityListResult.class);
        DatabaseHelper.getInstance(context).writeOverseaCityList(result);
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
