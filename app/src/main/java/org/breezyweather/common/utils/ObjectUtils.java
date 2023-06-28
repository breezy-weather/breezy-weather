package org.breezyweather.common.utils;

import javax.annotation.Nullable;

public class ObjectUtils {

    // TODO: Deprecate in favor of Kotlin
    public static int safeValueOf(@Nullable Integer integer) {
        if (integer == null) {
            return 0;
        }
        return integer;
    }
}
