package wangdaye.com.geometricweather.Data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    // data
    public static final String DATABASE_NAME = "MyDatabase.db";

    public static final String TABLE_LOCATION = "Location";
    public static final String COLUMN_LOCATION = "location";

    public static final String TABLE_WEATHER = "Weather";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_WEATHER = "weather";
    public static final String COLUMN_TEMP = "temp";

    public static final String CREATE_TABLE_LOCATION = "CREATE TABLE Location ("
            + "location     text    PRIMARY KEY)";

    public static final String CREATE_TABLE_WEATHER = "CREATE TABLE Weather ("
            + "location     text,"
            + "time         text,"
            + "weather      text,"
            + "temp         text)";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
