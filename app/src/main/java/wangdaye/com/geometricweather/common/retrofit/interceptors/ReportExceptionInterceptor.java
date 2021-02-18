package wangdaye.com.geometricweather.common.retrofit.interceptors;

import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import wangdaye.com.geometricweather.common.utils.helpters.BuglyHelper;

abstract class ReportExceptionInterceptor implements Interceptor {

    void handleException(Exception e) {
        e.printStackTrace();
        BuglyHelper.report(e);
    }

    Response nullResponse(Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(400)
                .message("Handle an error in GeometricWeather.")
                .body(null)
                .build();
    }
}
