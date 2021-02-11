package wangdaye.com.geometricweather.basic.models.options._utils;

import android.content.res.Resources;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;

public class Utils {

    @Nullable
    public static String getNameByValue(Resources res, String value,
                                        @ArrayRes int nameArrayId, @ArrayRes int valueArrayId) {
        String[] names = res.getStringArray(nameArrayId);
        String[] values = res.getStringArray(valueArrayId);

        for (int i = 0; i < values.length; i ++) {
            if (values[i].equals(value)) {
                return names[i];
            }
        }

        return null;
    }
}
