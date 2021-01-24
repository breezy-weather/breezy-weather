package wangdaye.com.geometricweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

import wangdaye.com.geometricweather.db.entity.AlertEntityDao;
import wangdaye.com.geometricweather.db.entity.ChineseCityEntityDao;
import wangdaye.com.geometricweather.db.entity.DailyEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoMaster;
import wangdaye.com.geometricweather.db.entity.HistoryEntityDao;
import wangdaye.com.geometricweather.db.entity.HourlyEntityDao;
import wangdaye.com.geometricweather.db.entity.LocationEntityDao;
import wangdaye.com.geometricweather.db.entity.MinutelyEntityDao;
import wangdaye.com.geometricweather.db.entity.WeatherEntityDao;

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
