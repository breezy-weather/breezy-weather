package wangdaye.com.geometricweather;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;

/**
 * Geometric realTimeWeather.
 * */

public class GeometricWeather extends Application {
    // data
    private List<GeoActivity> activityList;
    private boolean colorNavigationBar;
    private String language;

    /** <br> life cycle. */

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        instance = this;
        activityList = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        colorNavigationBar = sharedPreferences.getBoolean(getString(R.string.key_navigationBar_color), false);
        language = sharedPreferences.getString(getString(R.string.key_language), "follow_system");
    }

    /** <br> data. */

    public void addActivity(GeoActivity a) {
        activityList.add(a);
    }

    public void removeActivity() {
        activityList.remove(activityList.size() - 1);
    }

    public GeoActivity getTopActivity() {
        if (activityList.size() == 0) {
            return null;
        }
        return activityList.get(activityList.size() - 1);
    }

    public boolean isColorNavigationBar() {
        return colorNavigationBar;
    }

    public void setColorNavigationBar() {
        this.colorNavigationBar = !colorNavigationBar;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /** <br> singleton. */

    private static GeometricWeather instance;

    public static GeometricWeather getInstance() {
        return instance;
    }
}
