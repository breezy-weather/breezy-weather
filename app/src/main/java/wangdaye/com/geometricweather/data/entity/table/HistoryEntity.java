package wangdaye.com.geometricweather.data.entity.table;

import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.data.entity.model.History;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * History entity.
 * */

@Entity
public class HistoryEntity {
    // data
    @Id(autoincrement = true)
    public Long id;

    public String location;
    public String date;

    public int maxiTemp;
    public int miniTemp;

    @Generated(hash = 1014605983)
    public HistoryEntity(Long id, String location, String date, int maxiTemp,
            int miniTemp) {
        this.id = id;
        this.location = location;
        this.date = date;
        this.maxiTemp = maxiTemp;
        this.miniTemp = miniTemp;
    }

    @Generated(hash = 1235354573)
    public HistoryEntity() {
    }

    /** <br> life cycle. */

    public static HistoryEntity build(History h) {
        HistoryEntity entity = new HistoryEntity();
        entity.location = h.location;
        entity.date = h.date;
        entity.maxiTemp = h.maxiTemp;
        entity.miniTemp = h.miniTemp;
        return entity;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getMaxiTemp() {
        return this.maxiTemp;
    }

    public void setMaxiTemp(int maxiTemp) {
        this.maxiTemp = maxiTemp;
    }

    public int getMiniTemp() {
        return this.miniTemp;
    }

    public void setMiniTemp(int miniTemp) {
        this.miniTemp = miniTemp;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
