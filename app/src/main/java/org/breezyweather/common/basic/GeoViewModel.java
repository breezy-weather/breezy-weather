package org.breezyweather.common.basic;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

// TODO: Issue with getter on application when converted to Kotlin
public class GeoViewModel extends AndroidViewModel {

    private boolean mNewInstance;

    public GeoViewModel(@NonNull Application application) {
        super(application);
        mNewInstance = true;
    }

    public boolean checkIsNewInstance() {
        boolean result = mNewInstance;
        mNewInstance = false;
        return result;
    }
}
