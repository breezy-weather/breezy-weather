package wangdaye.com.geometricweather.Data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper.
 * */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    // data
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
        db.execSQL(CREATE_TABLE_INFO);
    }
}
