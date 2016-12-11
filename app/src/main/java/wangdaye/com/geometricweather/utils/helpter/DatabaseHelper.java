package wangdaye.com.geometricweather.utils.helpter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.data.entity.table.DaoMaster;
import wangdaye.com.geometricweather.data.entity.table.HistoryEntity;
import wangdaye.com.geometricweather.data.entity.table.HistoryEntityDao;
import wangdaye.com.geometricweather.data.entity.table.LocationEntity;
import wangdaye.com.geometricweather.data.entity.table.LocationEntityDao;
import wangdaye.com.geometricweather.data.entity.table.WeatherEntity;
import wangdaye.com.geometricweather.data.entity.table.WeatherEntityDao;

/**
 * Database helper
 * */

public class DatabaseHelper {
    // data
    private DaoMaster.DevOpenHelper helper;
    private final static String DATABASE_NAME = "Geometric_Weather_db";
    private final String LOCAL;

    /** <br> life cycle. */

    private DatabaseHelper(Context c) {
        helper = new DaoMaster.DevOpenHelper(c, DATABASE_NAME);
        LOCAL = c.getString(R.string.local);
    }

    /** <br> database. */

    private SQLiteDatabase getDatabase() {
        return helper.getWritableDatabase();
    }

    /** <br> locationNow. */

    // insert.

    public void insertLocation(Location l) {
        if (l == null) {
            return;
        }

        LocationEntity entity = searchLocationEntity(l.name);
        if (entity == null) {
            new DaoMaster(getDatabase())
                    .newSession()
                    .getLocationEntityDao()
                    .insert(LocationEntity.build(l));
        } else {
            entity.location = l.name;
            entity.realLocation = l.realName;
            updateLocation(entity);
        }
    }

    public void writeLocationList(List<Location> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        clearLocation();
        List<LocationEntity> entityList = new ArrayList<>();
        for (int i = 0; i < list.size(); i ++) {
            entityList.add(LocationEntity.build(list.get(i)));
        }
        new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .insertInTx(entityList);
    }

    // delete.

    public void deleteLocation(Location l) {
        if (l == null) {
            return;
        }

        LocationEntity entity = searchLocationEntity(l.name);
        if (entity != null) {
            new DaoMaster(getDatabase())
                    .newSession()
                    .getLocationEntityDao()
                    .delete(entity);
        }
    }

    public void clearLocation() {
        new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .deleteAll();
    }

    // update

    private void updateLocation(LocationEntity entity) {
        new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .update(entity);
    }

    // search.

    public Location searchLocation(String name) {
        LocationEntity entity = searchLocationEntity(name);
        if (entity == null) {
            return null;
        } else {
            return Location.build(entity);
        }
    }

    private LocationEntity searchLocationEntity(String name) {
        LocationEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao();

        QueryBuilder<LocationEntity> builder = dao.queryBuilder();
        builder.where(LocationEntityDao.Properties.Location.eq(name));

        List<LocationEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    public List<Location> readLocationList() {
        List<LocationEntity> entityList = new DaoMaster(getDatabase())
                .newSession()
                .getLocationEntityDao()
                .queryBuilder()
                .list();
        List<Location> locationList = new ArrayList<>();
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(
                    new Location(
                            entityList.get(i).location,
                            entityList.get(i).realLocation));
        }
        if (locationList.size() <= 0) {
            locationList.add(new Location(LOCAL, null));
        }
        return locationList;
    }

    /** <br> weather. */

    // insert.

    public void insertWeather(Weather w) {
        if (w == null) {
            return;
        }

        WeatherEntity entity = searchWeatherEntity(w.base.location);
        WeatherEntity newEntity = WeatherEntity.build(w);
        if (entity != null) {
            deleteWeather(entity);
        }
        new DaoMaster(getDatabase())
                .newSession()
                .getWeatherEntityDao()
                .insert(newEntity);
    }

    // delete.

    private void deleteWeather(WeatherEntity entity) {
        new DaoMaster(getDatabase())
                .newSession()
                .getWeatherEntityDao()
                .delete(entity);
    }

    // search.

    public Weather searchWeather(String realName) {
        WeatherEntity entity = searchWeatherEntity(realName);
        if (entity == null) {
            return null;
        } else {
            return Weather.build(entity);
        }
    }

    private WeatherEntity searchWeatherEntity(String realName) {
        WeatherEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getWeatherEntityDao();

        List<WeatherEntity> entityList = dao
                .queryBuilder()
                .where(WeatherEntityDao.Properties.Location.eq(realName))
                .list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    /** <br> history. */

    // insert.

    public void insertHistory(Weather w) {
        if (w == null) {
            return;
        }

        History yesterday = searchYesterdayHistory(w);
        clearLocationHistory(w);

        HistoryEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getHistoryEntityDao();
        if (yesterday != null) {
            dao.insert(HistoryEntity.build(yesterday));
        }
        dao.insert(HistoryEntity.build(History.build(w)));
    }

    // delete.

    private void clearLocationHistory(Weather w) {
        if (w == null) {
            return;
        }

        List<HistoryEntity> entityList = searchHistoryEntity(w);
        HistoryEntityDao dao = new DaoMaster(getDatabase())
                .newSession()
                .getHistoryEntityDao();
        for (int i = 0; i < entityList.size(); i ++) {
            dao.delete(entityList.get(i));
        }
    }

    @SuppressLint("SimpleDateFormat")
    public History searchYesterdayHistory(Weather w) {
        if (w == null) {
            return null;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(w.base.date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);

            HistoryEntityDao dao = new DaoMaster(getDatabase())
                    .newSession()
                    .getHistoryEntityDao();

            QueryBuilder<HistoryEntity> builder = dao.queryBuilder();
            builder.where(
                    HistoryEntityDao.Properties.Location.eq(w.base.location),
                    HistoryEntityDao.Properties.Date.eq(format.format(calendar.getTime())));

            List<HistoryEntity> entityList = builder.list();
            if (entityList == null || entityList.size() <= 0) {
                return null;
            } else {
                return History.build(entityList.get(0));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<HistoryEntity> searchHistoryEntity(Weather w) {
        if (w == null) {
            return new ArrayList<>();
        }

        return new DaoMaster(getDatabase())
                .newSession()
                .getHistoryEntityDao()
                .queryBuilder()
                .where(HistoryEntityDao.Properties.Location.eq(w.base.location))
                .list();
    }

    /** <br> singleton. */

    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                instance = new DatabaseHelper(c);
            }
        }
        return instance;
    }
}
