package wangdaye.com.geometricweather.basic.model.option.utils;

import android.content.Context;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;

public class OptionUtils {

    @Nullable
    public static String getNameByValue(Context context, String value,
                                        @ArrayRes int nameArrayId, @ArrayRes int valueArrayId) {
        String[] names = context.getResources().getStringArray(nameArrayId);
        String[] values = context.getResources().getStringArray(valueArrayId);

        for (int i = 0; i < values.length; i ++) {
            if (values[i].equals(value)) {
                return names[i];
            }
        }

        return null;
    }
}
