package wangdaye.com.geometricweather.data.entity.model;

import wangdaye.com.geometricweather.data.entity.table.HistoryEntity;

/**
 * History
 * */

public class History {
    // data
    public String location;
    public String date;

    public int maxiTemp;
    public int miniTemp;

    /** <br> life cycle. */

    public static History build(Weather weather) {
        History history = new History();
        history.location = weather.base.location;
        history.date = weather.base.date;
        history.maxiTemp = weather.dailyList.get(0).temps[0];
        history.miniTemp = weather.dailyList.get(0).temps[1];

        return history;
    }

    public static History build(HistoryEntity entity) {
        History history = new History();
        history.location = entity.location;
        history.date = entity.date;
        history.maxiTemp = entity.maxiTemp;
        history.miniTemp = entity.miniTemp;

        return history;
    }
}
