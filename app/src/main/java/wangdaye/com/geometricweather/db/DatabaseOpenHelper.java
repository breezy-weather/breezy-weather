package wangdaye.com.geometricweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

import wangdaye.com.geometricweather.db.entities.AlertEntityDao;
import wangdaye.com.geometricweather.db.entities.ChineseCityEntityDao;
import wangdaye.com.geometricweather.db.entities.DailyEntityDao;
import wangdaye.com.geometricweather.db.entities.DaoMaster;
import wangdaye.com.geometricweather.db.entities.HistoryEntityDao;
import wangdaye.com.geometricweather.db.entities.HourlyEntityDao;
import wangdaye.com.geometricweather.db.entities.LocationEntityDao;
import wangdaye.com.geometricweather.db.entities.MinutelyEntityDao;
import wangdaye.com.geometricweather.db.entities.WeatherEntityDao;

class DatabaseOpenHelper extends DaoMaster.OpenHelper {

    DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        if (oldVersion >= 53) {
            MigrationHelper.migrate(
                    db,
                    new MigrationHelper.ReCreateAllTableListener() {
                        @Override
                        public void onCreateAllTables(Database db, boolean ifNotExists) {
                            DaoMaster.createAllTables(db, ifNotExists);
                        }

                        @Override
                        public void onDropAllTables(Database db, boolean ifExists) {
                            DaoMaster.dropAllTables(db, ifExists);
                        }
                    },
                    AlertEntityDao.class,
                    ChineseCityEntityDao.class,
                    DailyEntityDao.class,
                    HistoryEntityDao.class,
                    HourlyEntityDao.class,
                    LocationEntityDao.class,
                    MinutelyEntityDao.class,
                    WeatherEntityDao.class
            );
        } else {
            DaoMaster.dropAllTables(db, true);
            onCreate(db);
        }
    }
}
