package org.breezyweather.common.retrofit;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Cache results service.
 **/
public class ClientCacheHelper {

    private static Cache clientCache = null;
    public static boolean createClientCache(File directory)
    {
        if (clientCache != null || directory == null || !directory.canWrite())
            return false;

        // cf. https://square.github.io/okhttp/features/caching/
        clientCache = new Cache(directory, 64L * 1024L * 1024L); // 64 MiB
        return true;
    }

    public static OkHttpClient.Builder getClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .cache(clientCache);
        return builder;
    }
}
