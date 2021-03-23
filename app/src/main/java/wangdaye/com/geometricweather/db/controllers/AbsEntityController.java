package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsEntityController {

    protected static <E> List<E> getNonNullList(@Nullable List<E> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
