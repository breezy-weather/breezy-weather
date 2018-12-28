package wangdaye.com.geometricweather.utils;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

public class GzipInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .build();
        return buildResponse(request, chain.proceed(request));
    }

    private Response buildResponse(Request request, Response response) throws IOException {
        // return response;

        ResponseBody body = response.body();
        if (body == null) {
            return response;
        }

        BufferedSource source = body.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        if ("gzip".equalsIgnoreCase(response.headers().get("Content-Encoding"))) {
            GzipSource gzippedResponseBody = null;
            try {
                gzippedResponseBody = new GzipSource(buffer.clone());
                buffer = new Buffer();
                buffer.writeAll(gzippedResponseBody);
            } finally {
                if (gzippedResponseBody != null) {
                    gzippedResponseBody.close();
                }
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