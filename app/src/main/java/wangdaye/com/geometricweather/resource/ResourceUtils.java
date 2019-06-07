package wangdaye.com.geometricweather.resource;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;

public class ResourceUtils {

    @AnyRes
    public static int getResId(Context context, String resName, String type) {
        try {
            return context.getClassLoader()
                    .loadClass(context.getPackageName() + ".R$" + type)
                    .getField(resName)
                    .getInt(null);
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    public static Uri getDrawableUri(String pkgName, String resType, String resName) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(pkgName)
                .appendPath(resType)
                .appendPath(resName)
                .build();
    }
}
