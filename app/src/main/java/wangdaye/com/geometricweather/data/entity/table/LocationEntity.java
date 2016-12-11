package wangdaye.com.geometricweather.data.entity.table;

import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.data.entity.model.Location;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Location entity.
 * */

@Entity
public class LocationEntity {
    // data
    @Id(autoincrement = true)
    public Long id;

    @Unique
    public String location;
    public String realLocation;

    @Generated(hash = 1589640411)
    public LocationEntity(Long id, String location, String realLocation) {
        this.id = id;
        this.location = location;
        this.realLocation = realLocation;
    }

    @Generated(hash = 1723987110)
    public LocationEntity() {
    }

    /** <br> life cycle. */

    public static LocationEntity build(Location l) {
        LocationEntity entity = new LocationEntity();
        entity.location = l.name;
        entity.realLocation = l.realName;
        return entity;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRealLocation() {
        return this.realLocation;
    }

    public void setRealLocation(String realLocation) {
        this.realLocation = realLocation;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
