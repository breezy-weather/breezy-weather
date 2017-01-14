package wangdaye.com.geometricweather;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Geometric realTimeWeather.
 * */

public class GeometricWeather extends Application {
    // data
    private List<GeoActivity> activityList;
    private boolean colorNavigationBar;
    private boolean fahrenheit;

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
        LanguageUtils.setLanguage(this, sharedPreferences.getString(getString(R.string.key_language), "follow_system"));
        fahrenheit = sharedPreferences.getBoolean(getString(R.string.key_fahrenheit), false);
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

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit() {
        this.fahrenheit = !fahrenheit;
    }

    /** <br> singleton. */

    private static GeometricWeather instance;

    public static GeometricWeather getInstance() {
        return instance;
    }
}
