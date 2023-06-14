package org.breezyweather.location.services;

import android.content.Context;

import android.text.TextUtils;
import androidx.annotation.NonNull;

import javax.inject.Inject;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.breezyweather.location.apis.BaiduIPLocationApi;
import org.breezyweather.location.json.BaiduIPLocationResult;
import org.breezyweather.common.rxjava.BaseObserver;
import org.breezyweather.common.rxjava.ObserverContainer;
import org.breezyweather.common.rxjava.SchedulerTransformer;
import org.breezyweather.settings.SettingsManager;

public class BaiduIPLocationService extends LocationService {

    private final BaiduIPLocationApi mApi;
    private final CompositeDisposable compositeDisposable;

    @Inject
    public BaiduIPLocationService(BaiduIPLocationApi api,
                                  CompositeDisposable disposable) {
        mApi = api;
        compositeDisposable = disposable;
    }

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback) {
        mApi.getLocation(SettingsManager.getInstance(context).getProviderBaiduIpLocationAk(), "gcj02")
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<BaiduIPLocationResult>() {
                    @Override
                    public void onSucceed(BaiduIPLocationResult baiduIPLocationResult) {
                        if (baiduIPLocationResult.getContent() == null
                                || baiduIPLocationResult.getContent().getPoint() == null
                                || TextUtils.isEmpty(baiduIPLocationResult.getContent().getPoint().getY())
                                || TextUtils.isEmpty(baiduIPLocationResult.getContent().getPoint().getX())) {
                            callback.onCompleted(null);
                        } else {
                            try {
                                Result result = new Result(
                                        Float.parseFloat(baiduIPLocationResult.getContent().getPoint().getY()),
                                        Float.parseFloat(baiduIPLocationResult.getContent().getPoint().getX())
                                );
                                callback.onCompleted(result);
                            } catch (Exception ignore) {
                                callback.onCompleted(null);
                            }
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.onCompleted(null);
                    }
                }));
    }

    @Override
    public void cancel() {
        compositeDisposable.clear();
    }

    @Override
    public boolean hasPermissions(Context context) {
        return true;
    }

    @Override
    public String[] getPermissions() {
        return new String[0];
    }
}
