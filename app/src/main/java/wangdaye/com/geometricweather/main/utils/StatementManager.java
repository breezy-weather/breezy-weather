package wangdaye.com.geometricweather.main.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class StatementManager {

    private boolean locationPermissionDeclared;
    private boolean backgroundLocationDeclared;

    private static final String SP_STATEMENT_RECORD = "statement_record";
    private static final String KEY_LOCATION_PERMISSION_DECLARED = "location_permission_declared";
    private static final String KEY_BACKGROUND_LOCATION_DECLARED = "background_location_declared";

    public StatementManager(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SP_STATEMENT_RECORD, Context.MODE_PRIVATE);

        locationPermissionDeclared = sharedPreferences.getBoolean(
                KEY_LOCATION_PERMISSION_DECLARED, false);

        backgroundLocationDeclared = sharedPreferences.getBoolean(
                KEY_BACKGROUND_LOCATION_DECLARED, false);
    }

    public boolean isLocationPermissionDeclared() {
        return locationPermissionDeclared;
    }

    public void setLocationPermissionDeclared(Context context) {
        locationPermissionDeclared = true;

        context.getSharedPreferences(SP_STATEMENT_RECORD, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_LOCATION_PERMISSION_DECLARED, true)
                .apply();
    }

    public boolean isBackgroundLocationDeclared() {
        return backgroundLocationDeclared;
    }

    public void setBackgroundLocationDeclared(Context context) {
        backgroundLocationDeclared = true;

        context.getSharedPreferences(SP_STATEMENT_RECORD, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_BACKGROUND_LOCATION_DECLARED, true)
                .apply();
    }
}
