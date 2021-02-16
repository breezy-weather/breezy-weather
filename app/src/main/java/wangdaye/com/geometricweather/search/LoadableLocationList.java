package wangdaye.com.geometricweather.search;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.resources.ListResource;

public class LoadableLocationList extends ListResource<Location> {

    public final Status status;
    public enum  Status {
        LOADING, ERROR, SUCCESS
    }

    public LoadableLocationList(@NonNull List<Location> dataList, Status status) {
        super(dataList);
        this.status = status;
    }
}
