package wangdaye.com.geometricweather.utils;

import android.content.Context;

import androidx.annotation.AnyRes;

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
}
