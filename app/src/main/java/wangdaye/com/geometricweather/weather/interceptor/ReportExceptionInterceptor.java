package wangdaye.com.geometricweather.weather.interceptor;

import com.tencent.bugly.crashreport.CrashReport;

import okhttp3.Interceptor;

public abstract class ReportExceptionInterceptor implements Interceptor {

    public void handleException(Exception e) {
        e.printStackTrace();
        CrashReport.postCatchedException(e);
    }
}
