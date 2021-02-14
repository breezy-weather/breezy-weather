package wangdaye.com.geometricweather.management;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;

public class ManagementFragmentRepository {
    
    private final ExecutorService mSingleThreadExecutor;

    public ManagementFragmentRepository() {
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    public void destroy() {
        mSingleThreadExecutor.shutdown();
    }

    public void readLocationList(Context context, AsyncHelper.Callback<List<Location>> callback) {
        AsyncHelper.runOnExecutor(emitter -> {
            List<Location> locationList = DatabaseHelper.getInstance(context).readLocationList();
            emitter.send(locationList, false);

            List<Location> list = new ArrayList<>(locationList);
            for (Location l : list) {
                l.setWeather(DatabaseHelper.getInstance(context).readWeather(l));
            }
            emitter.send(list, true);
        }, callback, mSingleThreadExecutor);
    }

    public void writeLocation(Context context, Location location) {
        AsyncHelper.runOnExecutor(() -> {
            DatabaseHelper.getInstance(context).writeLocation(location);
            if (location.getWeather() != null) {
                DatabaseHelper.getInstance(context).writeWeather(location, location.getWeather());
            }
        }, mSingleThreadExecutor);
    }

    public void writeLocationList(Context context, List<Location> locationList) {
        AsyncHelper.runOnExecutor(() -> DatabaseHelper.getInstance(context).writeLocationList(locationList),
                mSingleThreadExecutor);
    }

    public void writeLocationList(Context context, List<Location> locationList, int newIndex) {
        AsyncHelper.runOnExecutor(() -> {
            DatabaseHelper.getInstance(context).writeLocationList(locationList);

            Location newItem = locationList.get(newIndex);
            if (newItem.getWeather() != null) {
                DatabaseHelper.getInstance(context).writeWeather(newItem, newItem.getWeather());
            }
        }, mSingleThreadExecutor);
    }

    public void deleteLocation(Context context, Location location) {
        AsyncHelper.runOnExecutor(() -> {
            DatabaseHelper.getInstance(context).deleteLocation(location);
            if (location.getWeather() != null) {
                DatabaseHelper.getInstance(context).deleteWeather(location);
            }
        }, mSingleThreadExecutor);
    }
}
