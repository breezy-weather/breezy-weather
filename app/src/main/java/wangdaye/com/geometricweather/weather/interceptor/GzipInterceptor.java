package wangdaye.com.geometricweather.weather.interceptor;

import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

public class GzipInterceptor extends ReportExceptionInterceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request()
                .newBuilder()
                .build();
        try {
            return buildResponse(request, chain.proceed(request));
        } catch (Exception e) {
            return nullResponse(request);
        }
    }

    private Response buildResponse(Request request, Response response) throws Exception {
        ResponseBody body = response.body();
        if (body == null) {
            return response;
        }

        BufferedSource source = body.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        if ("gzip".equalsIgnoreCase(response.headers().get("Content-Encoding"))) {
            try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                buffer = new Buffer();
                buffer.writeAll(gzippedResponseBody);
            }
        }

        Charset charset = UTF8;
        MediaType contentType = body.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }

        String bodyString = "";
        if (charset != null) {
            bodyString = buffer.clone().readString(charset);
        }

        return new Response.Builder()
                .addHeader("Content-Type", "application/json")
                .code(response.code())
                .body(ResponseBody.create(body.contentType(), bodyString))
                .message(response.message())
                .request(request)
                .protocol(Protocol.HTTP_2)
                .build();
    }
}