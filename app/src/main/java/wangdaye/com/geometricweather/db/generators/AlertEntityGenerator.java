package wangdaye.com.geometricweather.db.generators;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.AlertEntity;

public class AlertEntityGenerator {

    public static AlertEntity generate(String cityId, WeatherSource source, Alert alert) {
        AlertEntity entity = new AlertEntity();

        entity.cityId = cityId;
        entity.weatherSource = new WeatherSourceConverter().convertToDatabaseValue(source);

        entity.alertId = alert.getAlertId();
        entity.startDate = alert.getStartDate();
        entity.endDate = alert.getEndDate();

        entity.description = alert.getDescription();
        entity.content = alert.getContent();

        entity.type = alert.getType();
        entity.priority = alert.getPriority();
        entity.color = alert.getColor();

        return entity;
    }

    public static List<AlertEntity> generate(String cityId, WeatherSource source,
                                             List<Alert> alertList) {
        List<AlertEntity> entityList = new ArrayList<>(alertList.size());
        for (Alert alert : alertList) {
            entityList.add(generate(cityId, source, alert));
        }
        return entityList;
    }

    public static Alert generate(AlertEntity entity) {
        return new Alert(
                entity.alertId,
                entity.startDate,
                entity.endDate,
                entity.description,
                entity.content,
                entity.type,
                entity.priority,
                entity.color
        );
    }

    public static List<Alert> generate(List<AlertEntity> entityList) {
        List<Alert> dailyList = new ArrayList<>(entityList.size());
        for (AlertEntity entity : entityList) {
            dailyList.add(generate(entity));
        }
        return dailyList;
    }
}
