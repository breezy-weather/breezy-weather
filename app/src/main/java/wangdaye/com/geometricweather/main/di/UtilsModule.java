package wangdaye.com.geometricweather.main.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ActivityContext;
import wangdaye.com.geometricweather.main.utils.StatementManager;

@InstallIn(ActivityComponent.class)
@Module
public class UtilsModule {

    @Provides
    public StatementManager provideStatementManager(@ActivityContext Context context) {
        return StatementManager.getInstance(context);
    }
}
