package org.breezyweather.main.utils;

import android.content.Context;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import org.breezyweather.settings.ConfigStore;

public class StatementManager {

    private ConfigStore config;
    private boolean locationPermissionDeclared;
    private boolean backgroundLocationDeclared;
    private boolean postNotificationRequired;

    private static final String SP_STATEMENT_RECORD = "statement_record";
    private static final String KEY_LOCATION_PERMISSION_DECLARED = "location_permission_declared";
    private static final String KEY_BACKGROUND_LOCATION_DECLARED = "background_location_declared";
    private static final String KEY_POST_NOTIFICATION_REQUIRED = "post_notification_required";

    @Inject
    public StatementManager(@ApplicationContext Context context) {
        config = new ConfigStore(context, SP_STATEMENT_RECORD);

        locationPermissionDeclared = config.getBoolean(
                KEY_LOCATION_PERMISSION_DECLARED, false);

        backgroundLocationDeclared = config.getBoolean(
                KEY_BACKGROUND_LOCATION_DECLARED, false);

        postNotificationRequired = config.getBoolean(
                KEY_POST_NOTIFICATION_REQUIRED, false);
    }

    public boolean isLocationPermissionDeclared() {
        return locationPermissionDeclared;
    }

    public void setLocationPermissionDeclared() {
        locationPermissionDeclared = true;

        config.edit()
            .putBoolean(KEY_LOCATION_PERMISSION_DECLARED, true)
            .apply();
    }

    public boolean isBackgroundLocationDeclared() {
        return backgroundLocationDeclared;
    }

    public void setBackgroundLocationDeclared() {
        backgroundLocationDeclared = true;

        config.edit()
            .putBoolean(KEY_BACKGROUND_LOCATION_DECLARED, true)
            .apply();
    }

    public boolean isPostNotificationRequired() {
        return postNotificationRequired;
    }

    public void setPostNotificationRequired() {
        postNotificationRequired = true;

        config.edit()
            .putBoolean(KEY_POST_NOTIFICATION_REQUIRED, true)
            .apply();
    }
}
