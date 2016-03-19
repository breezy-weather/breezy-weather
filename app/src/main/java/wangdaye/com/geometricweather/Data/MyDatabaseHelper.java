package wangdaye.com.geometricweather.Data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Database helper.
 * */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    // data
    public static final int VERSION_CODE = 3;

    public static final String DATABASE_NAME = "MyDatabase.db";

    public static final String TABLE_LOCATION = "Location";
    public static final String COLUMN_LOCATION = "location";

    public static final String TABLE_WEATHER = "Weather";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_WEATHER = "weather";
    public static final String COLUMN_TEMP = "temp";

    public static final String TABLE_INFO = "Info";
    public static final String COLUMN_REAL_LOCATION = "realLocation";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_MOON = "moon";
    public static final String COLUMN_REFRESH_TIME = "refreshTime";
    public static final String COLUMN_WEATHER_NOW = "weatherNow";
    public static final String COLUMN_WEATHER_KIND_NOW = "weatherKindNow";
    public static final String COLUMN_TEMP_NOW = "tempNow";
    public static final String COLUMN_WEEK_1 =  "week1";
    public static final String COLUMN_WEEK_2 =  "week2";
    public static final String COLUMN_WEEK_3 =  "week3";
    public static final String COLUMN_WEEK_4 =  "week4";
    public static final String COLUMN_WEEK_5 =  "week5";
    public static final String COLUMN_WEEK_6 =  "week6";
    public static final String COLUMN_WEEK_7 =  "week7";
    public static final String COLUMN_WEATHER_1 = "weather1";
    public static final String COLUMN_WEATHER_2 = "weather2";
    public static final String COLUMN_WEATHER_3 = "weather3";
    public static final String COLUMN_WEATHER_4 = "weather4";
    public static final String COLUMN_WEATHER_5 = "weather5";
    public static final String COLUMN_WEATHER_6 = "weather6";
    public static final String COLUMN_WEATHER_7 = "weather7";
    public static final String COLUMN_WEATHER_KIND_1 = "weatherKind1";
    public static final String COLUMN_WEATHER_KIND_2 = "weatherKind2";
    public static final String COLUMN_WEATHER_KIND_3 = "weatherKind3";
    public static final String COLUMN_WEATHER_KIND_4 = "weatherKind4";
    public static final String COLUMN_WEATHER_KIND_5 = "weatherKind5";
    public static final String COLUMN_WEATHER_KIND_6 = "weatherKind6";
    public static final String COLUMN_WEATHER_KIND_7 = "weatherKind7";
    public static final String COLUMN_WIND_DIR_1 = "windDir1";
    public static final String COLUMN_WIND_DIR_2 = "windDir2";
    public static final String COLUMN_WIND_DIR_3 = "windDir3";
    public static final String COLUMN_WIND_DIR_4 = "windDir4";
    public static final String COLUMN_WIND_DIR_5 = "windDir5";
    public static final String COLUMN_WIND_DIR_6 = "windDir6";
    public static final String COLUMN_WIND_DIR_7 = "windDir7";
    public static final String COLUMN_WIND_LEVEL_1 = "windLevel1";
    public static final String COLUMN_WIND_LEVEL_2 = "windLevel2";
    public static final String COLUMN_WIND_LEVEL_3 = "windLevel3";
    public static final String COLUMN_WIND_LEVEL_4 = "windLevel4";
    public static final String COLUMN_WIND_LEVEL_5 = "windLevel5";
    public static final String COLUMN_WIND_LEVEL_6 = "windLevel6";
    public static final String COLUMN_WIND_LEVEL_7 = "windLevel7";
    public static final String COLUMN_MAXI_TEMP_1 = "maxiTemp1";
    public static final String COLUMN_MAXI_TEMP_2 = "maxiTemp2";
    public static final String COLUMN_MAXI_TEMP_3 = "maxiTemp3";
    public static final String COLUMN_MAXI_TEMP_4 = "maxiTemp4";
    public static final String COLUMN_MAXI_TEMP_5 = "maxiTemp5";
    public static final String COLUMN_MAXI_TEMP_6 = "maxiTemp6";
    public static final String COLUMN_MAXI_TEMP_7 = "maxiTemp7";
    public static final String COLUMN_MINI_TEMP_1 = "miniTemp1";
    public static final String COLUMN_MINI_TEMP_2 = "miniTemp2";
    public static final String COLUMN_MINI_TEMP_3 = "miniTemp3";
    public static final String COLUMN_MINI_TEMP_4 = "miniTemp4";
    public static final String COLUMN_MINI_TEMP_5 = "miniTemp5";
    public static final String COLUMN_MINI_TEMP_6 = "miniTemp6";
    public static final String COLUMN_MINI_TEMP_7 = "miniTemp7";
    public static final String COLUMN_WIND_TITLE = "windTitle";
    public static final String COLUMN_WIND_INFO = "windInfo";
    public static final String COLUMN_PM_TITLE = "pmTitle";
    public static final String COLUMN_PM_INFO = "pmInfo";
    public static final String COLUMN_HUM_TITLE = "humTitle";
    public static final String COLUMN_HUM_INFO = "humInfo";
    public static final String COLUMN_UV_TITLE = "uvTitle";
    public static final String COLUMN_UV_INFO = "uvInfo";
    public static final String COLUMN_DRESS_TITLE = "dressTitle";
    public static final String COLUMN_DRESS_INFO = "dressInfo";
    public static final String COLUMN_COLD_TITLE = "coldTitle";
    public static final String COLUMN_COLD_INFO = "coldInfo";
    public static final String COLUMN_AIR_TITLE = "airTitle";
    public static final String COLUMN_AIR_INFO = "airInfo";
    public static final String COLUMN_WASH_CAR_TITLE = "washCarTitle";
    public static final String COLUMN_WASH_CAR_INFO = "washCarInfo";
    public static final String COLUMN_EXERCISE_TITLE = "exerciseTitle";
    public static final String COLUMN_EXERCISE_INFO = "exerciseInfo";

    public static final String CREATE_TABLE_LOCATION = "CREATE TABLE Location ("
            + "location     text    PRIMARY KEY)";

    public static final String CREATE_TABLE_WEATHER = "CREATE TABLE Weather ("
            + "location     text,"
            + "time         text,"
            + "weather      text,"
            + "temp         text)";

    public static final String CREATE_TABLE_INFO = "CREATE TABLE Info ("
            + "realLocation     text    PRIMARY KEY,"
            + "date             text,"
            + "moon             text,"
            + "refreshTime      text,"
            + "location         text,"
            + "weatherNow       text,"
            + "weatherKindNow   text,"
            + "tempNow          text,"
            + "week1            text,"
            + "week2            text,"
            + "week3            text,"
            + "week4            text,"
            + "week5            text,"
            + "week6            text,"
            + "week7            text,"
            + "weather1         text,"
            + "weather2         text,"
            + "weather3         text,"
            + "weather4         text,"
            + "weather5         text,"
            + "weather6         text,"
            + "weather7         text,"
            + "weatherKind1     text,"
            + "weatherKind2     text,"
            + "weatherKind3     text,"
            + "weatherKind4     text,"
            + "weatherKind5     text,"
            + "weatherKind6     text,"
            + "weatherKind7     text,"
            + "windDir1         text,"
            + "windDir2         text,"
            + "windDir3         text,"
            + "windDir4         text,"
            + "windDir5         text,"
            + "windDir6         text,"
            + "windDir7         text,"
            + "windLevel1       text,"
            + "windLevel2       text,"
            + "windLevel3       text,"
            + "windLevel4       text,"
            + "windLevel5       text,"
            + "windLevel6       text,"
            + "windLevel7       text,"
            + "maxiTemp1        text,"
            + "maxiTemp2        text,"
            + "maxiTemp3        text,"
            + "maxiTemp4        text,"
            + "maxiTemp5        text,"
            + "maxiTemp6        text,"
            + "maxiTemp7        text,"
            + "miniTemp1        text,"
            + "miniTemp2        text,"
            + "miniTemp3        text,"
            + "miniTemp4        text,"
            + "miniTemp5        text,"
            + "miniTemp6        text,"
            + "miniTemp7        text,"
            + "windTitle        text,"
            + "windInfo         text,"
            + "pmTitle          text,"
            + "pmInfo           text,"
            + "humTitle         text,"
            + "humInfo          text,"
            + "uvTitle          text,"
            + "uvInfo           text,"
            + "dressTitle       text,"
            + "dressInfo        text,"
            + "coldTitle        text,"
            + "coldInfo         text,"
            + "airTitle         text,"
            + "airInfo          text,"
            + "washCarTitle     text,"
            + "washCarInfo      text,"
            + "exerciseTitle    text,"
            + "exerciseInfo     text)";

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOCATION);
        db.execSQL(CREATE_TABLE_WEATHER);
        db.execSQL(CREATE_TABLE_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Info");
        db.execSQL(CREATE_TABLE_INFO);
    }

    @SuppressLint("SimpleDateFormat")
    public static void writeTodayWeather(Context context, WeatherInfoToShow info) {
        // get yesterday date.
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String yesterdayDate = simpleDateFormat.format(date);

        Weather yesterdayWeather = null;
        boolean haveYesterdayData = false;

        // init database.
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                MyDatabaseHelper.VERSION_CODE);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // read yesterday weather.
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_WEATHER,
                null,
                MyDatabaseHelper.COLUMN_LOCATION + " = '" + info.location
                        + "' AND "
                        + MyDatabaseHelper.COLUMN_TIME + " = '" + yesterdayDate + "'",
                null,
                null,
                null,
                null);
        if(cursor.moveToFirst()) {
            do {
                String locationText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                String weatherText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER));
                String tempText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TEMP));
                String timeText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TIME));
                yesterdayWeather = new Weather(locationText, weatherText, tempText, timeText);
                haveYesterdayData = true;
            } while (cursor.moveToNext());
        }
        cursor.close();

        // delete all weather data from param location.
        database.delete(MyDatabaseHelper.TABLE_WEATHER,
                MyDatabaseHelper.COLUMN_LOCATION + " = ?",
                new String[]{info.location});

        // write weather data from today and yesterday.
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_LOCATION, info.location);
        values.put(MyDatabaseHelper.COLUMN_WEATHER, info.weatherNow);
        values.put(MyDatabaseHelper.COLUMN_TEMP, info.miniTemp[0] + "/" + info.maxiTemp[0]);
        values.put(MyDatabaseHelper.COLUMN_TIME, info.date);
        database.insert(MyDatabaseHelper.TABLE_WEATHER, null, values);
        values.clear();
        if (haveYesterdayData) {
            values.put(MyDatabaseHelper.COLUMN_LOCATION, yesterdayWeather.location);
            values.put(MyDatabaseHelper.COLUMN_WEATHER, yesterdayWeather.weather);
            values.put(MyDatabaseHelper.COLUMN_TEMP, yesterdayWeather.temp);
            values.put(MyDatabaseHelper.COLUMN_TIME, yesterdayWeather.time);
            database.insert(MyDatabaseHelper.TABLE_WEATHER, null, values);
            values.clear();
        }
        database.close();
    }

    @SuppressLint("SimpleDateFormat")
    public static int[] readYesterdayWeather(Context context, WeatherInfoToShow info) {
        // get yesterday date.
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String yesterdayDate = simpleDateFormat.format(date);

        Weather yesterdayWeather = null;
        boolean haveYesterdayData = false;

        // init database.
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                MyDatabaseHelper.VERSION_CODE);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // read yesterday weather.
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_WEATHER,
                null,
                MyDatabaseHelper.COLUMN_LOCATION + " = '" + info.location
                        + "' AND "
                        + MyDatabaseHelper.COLUMN_TIME + " = '" + yesterdayDate + "'",
                null,
                null,
                null,
                null);
        if(cursor.moveToFirst()) {
            do {
                String locationText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                String weatherText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER));
                String tempText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TEMP));
                String timeText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TIME));
                yesterdayWeather = new Weather(locationText, weatherText, tempText, timeText);
                haveYesterdayData = true;
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        if (haveYesterdayData) {
            String[] yesterdayTemp = yesterdayWeather.temp.split("/");
            return new int[] {Integer.parseInt(yesterdayTemp[0]), Integer.parseInt(yesterdayTemp[1])};
        } else {
            return null;
        }
    }

    public static void writeWeatherInfo(Context context, String location, WeatherInfoToShow info) {
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                MyDatabaseHelper.VERSION_CODE);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.delete(MyDatabaseHelper.TABLE_INFO,
                MyDatabaseHelper.COLUMN_REAL_LOCATION + " = ?",
                new String[]{location});

        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_REAL_LOCATION, location);
        values.put(MyDatabaseHelper.COLUMN_DATE, info.date);
        values.put(MyDatabaseHelper.COLUMN_MOON, info.moon);
        values.put(MyDatabaseHelper.COLUMN_REFRESH_TIME, info.refreshTime);
        values.put(MyDatabaseHelper.COLUMN_LOCATION, info.location);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_NOW, info.weatherNow);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_NOW, info.weatherKindNow);
        values.put(MyDatabaseHelper.COLUMN_TEMP_NOW, info.tempNow);
        values.put(MyDatabaseHelper.COLUMN_WEEK_1, info.week[0]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_2, info.week[1]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_3, info.week[2]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_4, info.week[3]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_5, info.week[4]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_6, info.week[5]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_7, info.week[6]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_1, info.weather[0]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_2, info.weather[1]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_3, info.weather[2]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_4, info.weather[3]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_5, info.weather[4]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_6, info.weather[5]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_7, info.weather[6]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_1, info.weatherKind[0]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_2, info.weatherKind[1]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_3, info.weatherKind[2]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_4, info.weatherKind[3]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_5, info.weatherKind[4]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_6, info.weatherKind[5]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_7, info.weatherKind[6]);
        values.put(MyDatabaseHelper.COLUMN_WIND_DIR_1, info.windDir[0]);
        values.put(MyDatabaseHelper.COLUMN_WIND_DIR_2, info.windDir[1]);
        values.put(MyDatabaseHelper.COLUMN_WIND_DIR_3, info.windDir[2]);
        values.put(MyDatabaseHelper.COLUMN_WIND_DIR_4, info.windDir[3]);
        values.put(MyDatabaseHelper.COLUMN_WIND_DIR_5, info.windDir[4]);
        values.put(MyDatabaseHelper.COLUMN_WIND_DIR_6, info.windDir[5]);
        values.put(MyDatabaseHelper.COLUMN_WIND_DIR_7, info.windDir[6]);
        values.put(MyDatabaseHelper.COLUMN_WIND_LEVEL_1, info.windLevel[0]);
        values.put(MyDatabaseHelper.COLUMN_WIND_LEVEL_2, info.windLevel[1]);
        values.put(MyDatabaseHelper.COLUMN_WIND_LEVEL_3, info.windLevel[2]);
        values.put(MyDatabaseHelper.COLUMN_WIND_LEVEL_4, info.windLevel[3]);
        values.put(MyDatabaseHelper.COLUMN_WIND_LEVEL_5, info.windLevel[4]);
        values.put(MyDatabaseHelper.COLUMN_WIND_LEVEL_6, info.windLevel[5]);
        values.put(MyDatabaseHelper.COLUMN_WIND_LEVEL_7, info.windLevel[6]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_1, info.maxiTemp[0]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_2, info.maxiTemp[1]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_3, info.maxiTemp[2]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_4, info.maxiTemp[3]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_5, info.maxiTemp[4]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_6, info.maxiTemp[5]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_7, info.maxiTemp[6]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_1, info.miniTemp[0]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_2, info.miniTemp[1]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_3, info.miniTemp[2]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_4, info.miniTemp[3]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_5, info.miniTemp[4]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_6, info.miniTemp[5]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_7, info.miniTemp[6]);
        values.put(MyDatabaseHelper.COLUMN_WIND_TITLE, info.windTitle);
        values.put(MyDatabaseHelper.COLUMN_WIND_INFO, info.windInfo);
        values.put(MyDatabaseHelper.COLUMN_PM_TITLE, info.pmTitle);
        values.put(MyDatabaseHelper.COLUMN_PM_INFO, info.pmInfo);
        values.put(MyDatabaseHelper.COLUMN_HUM_TITLE, info.humTitle);
        values.put(MyDatabaseHelper.COLUMN_HUM_INFO, info.humInfo);
        values.put(MyDatabaseHelper.COLUMN_UV_TITLE, info.uvTitle);
        values.put(MyDatabaseHelper.COLUMN_UV_INFO, info.uvInfo);
        values.put(MyDatabaseHelper.COLUMN_DRESS_TITLE, info.dressTitle);
        values.put(MyDatabaseHelper.COLUMN_DRESS_INFO, info.dressInfo);
        values.put(MyDatabaseHelper.COLUMN_COLD_TITLE, info.coldTitle);
        values.put(MyDatabaseHelper.COLUMN_COLD_INFO, info.coldInfo);
        values.put(MyDatabaseHelper.COLUMN_AIR_TITLE, info.airTitle);
        values.put(MyDatabaseHelper.COLUMN_AIR_INFO, info.airInfo);
        values.put(MyDatabaseHelper.COLUMN_WASH_CAR_TITLE, info.washCarTitle);
        values.put(MyDatabaseHelper.COLUMN_WASH_CAR_INFO, info.washCarInfo);
        values.put(MyDatabaseHelper.COLUMN_EXERCISE_TITLE, info.exerciseTitle);
        values.put(MyDatabaseHelper.COLUMN_EXERCISE_INFO, info.exerciseInfo);
        database.insert(MyDatabaseHelper.TABLE_INFO, null, values);
        values.clear();
        database.close();
    }

    public static WeatherInfoToShow readWeatherInfo(Context context, String location) {
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                MyDatabaseHelper.VERSION_CODE);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        WeatherInfoToShow info = new WeatherInfoToShow();
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_INFO,
                null,
                MyDatabaseHelper.COLUMN_REAL_LOCATION + " = '" + location + "'",
                null,
                null,
                null,
                null);
        if(cursor.moveToFirst()) {
            do {
                info.date = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DATE));
                info.moon = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MOON));
                info.refreshTime = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_REFRESH_TIME));
                info.location = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                info.weatherNow = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_NOW));
                info.weatherKindNow = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_NOW));
                info.tempNow = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TEMP_NOW));
                info.week = new String[7];
                info.week[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_1));
                info.week[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_2));
                info.week[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_3));
                info.week[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_4));
                info.week[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_5));
                info.week[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_6));
                info.week[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_7));
                info.weather = new String[7];
                info.weather[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_1));
                info.weather[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_2));
                info.weather[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_3));
                info.weather[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_4));
                info.weather[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_5));
                info.weather[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_6));
                info.weather[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_7));
                info.weatherKind = new String[7];
                info.weatherKind[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_1));
                info.weatherKind[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_2));
                info.weatherKind[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_3));
                info.weatherKind[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_4));
                info.weatherKind[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_5));
                info.weatherKind[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_6));
                info.weatherKind[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_7));
                info.windDir = new String[7];
                info.windDir[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_DIR_1));
                info.windDir[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_DIR_2));
                info.windDir[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_DIR_3));
                info.windDir[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_DIR_4));
                info.windDir[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_DIR_5));
                info.windDir[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_DIR_6));
                info.windDir[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_DIR_7));
                info.windLevel = new String[7];
                info.windLevel[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_LEVEL_1));
                info.windLevel[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_LEVEL_2));
                info.windLevel[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_LEVEL_3));
                info.windLevel[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_LEVEL_4));
                info.windLevel[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_LEVEL_5));
                info.windLevel[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_LEVEL_6));
                info.windLevel[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_LEVEL_7));
                info.maxiTemp = new String[7];
                info.maxiTemp[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_1));
                info.maxiTemp[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_2));
                info.maxiTemp[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_3));
                info.maxiTemp[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_4));
                info.maxiTemp[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_5));
                info.maxiTemp[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_6));
                info.maxiTemp[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_7));
                info.miniTemp = new String[7];
                info.miniTemp[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_1));
                info.miniTemp[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_2));
                info.miniTemp[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_3));
                info.miniTemp[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_4));
                info.miniTemp[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_5));
                info.miniTemp[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_6));
                info.miniTemp[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_7));
                info.windTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_TITLE));
                info.windInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_INFO));
                info.pmTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PM_TITLE));
                info.pmInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PM_INFO));
                info.humTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_HUM_TITLE));
                info.humInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_HUM_INFO));
                info.uvTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_UV_TITLE));
                info.uvInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_UV_INFO));
                info.dressTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DRESS_TITLE));
                info.dressInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DRESS_INFO));
                info.coldTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLD_TITLE));
                info.coldInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLD_INFO));
                info.airTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_AIR_TITLE));
                info.airInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_AIR_INFO));
                info.washCarTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WASH_CAR_TITLE));
                info.washCarInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WASH_CAR_INFO));
                info.exerciseTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_EXERCISE_TITLE));
                info.exerciseInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_EXERCISE_INFO));
            } while (cursor.moveToNext());
        } else {
            return null;
        }
        cursor.close();
        database.close();

        return info;
    }
}
