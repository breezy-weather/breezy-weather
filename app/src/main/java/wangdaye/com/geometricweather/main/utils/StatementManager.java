package wangdaye.com.geometricweather.main.utils;

import android.content.Context;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import wangdaye.com.geometricweather.settings.ConfigStore;

public class StatementManager {

    private boolean locationPermissionDeclared;
    private boolean backgroundLocationDeclared;

    private static final String SP_STATEMENT_RECORD = "statement_record";
    private static final String KEY_LOCATION_PERMISSION_DECLARED = "location_permission_declared";
    private static final String KEY_BACKGROUND_LOCATION_DECLARED = "background_location_declared";

    @Inject
    public StatementManager(@ApplicationContext Context context) {
        ConfigStore config = ConfigStore.getInstance(context, SP_STATEMENT_RECORD);

        locationPermissionDeclared = config.getBoolean(
                KEY_LOCATION_PERMISSION_DECLARED, false);

        backgroundLocationDeclared = config.getBoolean(
                KEY_BACKGROUND_LOCATION_DECLARED, false);
    }

    public boolean isLocationPermissionDeclared() {
        return locationPermissionDeclared;
    }

    public void setLocationPermissionDeclared(Context context) {
        locationPermissionDeclared = true;

        ConfigStore.getInstance(context, SP_STATEMENT_RECORD)
                .edit()
                .putBoolean(KEY_LOCATION_PERMISSION_DECLARED, true)
                .apply();
    }

    public boolean isBackgroundLocationDeclared() {
        return backgroundLocationDeclared;
    }

    public void setBackgroundLocationDeclared(Context context) {
        backgroundLocationDeclared = true;

        ConfigStore.getInstance(context, SP_STATEMENT_RECORD)
                .edit()
                .putBoolean(KEY_BACKGROUND_LOCATION_DECLARED, true)
                .apply();
    }
}
