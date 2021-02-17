package wangdaye.com.geometricweather.common.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import io.reactivex.disposables.CompositeDisposable;

@InstallIn(ApplicationComponent.class)
@Module
public class RxModule {

    @Provides
    public CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }
}
