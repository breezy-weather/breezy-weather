package wangdaye.com.geometricweather.manage.model;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.resource.ListResource;

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
