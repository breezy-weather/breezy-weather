package wangdaye.com.geometricweather.management;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;

public class ManagementFragmentRepository {
    
    private final Executor mSingleThreadExecutor;

    public ManagementFragmentRepository() {
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
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

    public void readLocationList(Context context, List<Location> locationList, 
                                 AsyncHelper.Callback<List<Location>> callback) {
        AsyncHelper.runOnExecutor(emitter -> {
            List<Location> list = new ArrayList<>(locationList);
            for (Location l : list) {
                if (l.getWeather() == null) {
                    l.setWeather(DatabaseHelper.getInstance(context).readWeather(l));
                }
            }
            emitter.send(list, true);
        }, callback, mSingleThreadExecutor);
    }
    
    public void readAppendLocation(Context context, List<Location> oldList, 
                                   AsyncHelper.Callback<List<Location>> callback) {
        AsyncHelper.runOnExecutor(emitter -> {
            List<Location> newList = DatabaseHelper.getInstance(context).readLocationList();

            for (Location newOne : newList) {
                boolean hasWeather = false;
                for (Location oldOne : oldList) {
                    if (newOne.equals(oldOne)) {
                        hasWeather = true;
                        newOne.setWeather(oldOne.getWeather());
                        break;
                    }
                }
                if (!hasWeather) {
                    newOne.setWeather(DatabaseHelper.getInstance(context).readWeather(newOne));
                }
            }

            emitter.send(newList, true);
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
